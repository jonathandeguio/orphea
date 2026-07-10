import os
import requests
from jupyter_server.auth import Authorizer, IdentityProvider
from tornado.web import RequestHandler
import logging
import uuid
import time

# Setup logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


class SessionManager:
    SESSION_EXPIRY_TIME = 36000  # Session expiry time in seconds (1 hour)
    
    def __init__(self):
        # In-memory session store. You could replace this with a persistent store like Redis.
        self.session_store = {}

    def create_session(self, user_info):
        """
        Create a new session for the authenticated user and return the session ID.
        """
        session_id = str(uuid.uuid4())  # Generate a unique session ID
        self.session_store[session_id] = {
            'user_info': user_info,
            'created_at': time.time()  # Store the current time as session creation time
        }
        logger.info(f"Created session for user: {user_info['username']}")
        return session_id

    def retrieve_session(self, session_id):
        """
        Retrieve session data by session ID.
        """
        return self.session_store.get(session_id)

    def is_session_valid(self, session_id):
        """
        Validate if the session is still active and not expired.
        """
        session_data = self.session_store.get(session_id)
        if session_data:
            # Check if the session has expired
            if time.time() - session_data['created_at'] < self.SESSION_EXPIRY_TIME:
                logger.info(f"Session {session_id} is valid.")
                return True
            else:
                logger.info(f"Session {session_id} has expired.")
                self.delete_session(session_id)
        return False

    def get_user_from_session(self, session_id):
        """
        Return user information if the session is valid, otherwise return None.
        """
        session_data = self.retrieve_session(session_id)
        if session_data and self.is_session_valid(session_id):
            return session_data['user_info']
        return None

    def delete_session(self, session_id):
        """
        Delete the session by session ID.
        """
        if session_id in self.session_store:
            logger.info(f"Deleting session {session_id}")
            del self.session_store[session_id]

    def refresh_session(self, session_id):
        """
        Refresh the session to extend its expiration time.
        """
        session_data = self.session_store.get(session_id)
        if session_data:
            session_data['created_at'] = time.time()  # Reset the creation time to now
            logger.info(f"Session {session_id} has been refreshed.")

    def clear_expired_sessions(self):
        """
        Remove all expired sessions from the session store.
        """
        expired_sessions = [sid for sid, data in self.session_store.items()
                            if time.time() - data['created_at'] >= self.SESSION_EXPIRY_TIME]
        for session_id in expired_sessions:
            self.delete_session(session_id)

    def set_session_cookie(self, handler: RequestHandler, session_id):
        """
        Set the session ID in a secure cookie.
        """
        logger.info(f"Setting session cookie for session ID: {session_id}")
        handler.set_cookie('session_id', session_id, secure=True, httponly=True, samesite='Lax')

    def get_session_id_from_cookie(self, handler: RequestHandler):
        """
        Retrieve the session ID from the cookie.
        """
        session_id = handler.get_cookie('session_id')
        logger.info(f"Session ID retrieved from cookie: {session_id}")
        return session_id

class CustomIdentityProvider(IdentityProvider):
    def __init__(self, *args, **kwargs):
        # Pass arguments to the parent class (IdentityProvider)
        super().__init__(*args, **kwargs)

        # Initialize the session manager
        self.session_manager = SessionManager()

    def get_user(self, handler: RequestHandler):
        logger.info("Entering get_user method")

        # Try to get the token from the query parameter
        token = handler.get_query_argument('token', None)
        logger.info(f"Token received from query: {token}")

        # If token is not found, check if it's in the headers
        if not token:
            token = handler.request.headers.get('Authorization', None)
            logger.info(f"Token received from headers: {token}")

            # If it's a Bearer token in the Authorization header, remove 'Bearer ' prefix
            if token and token.startswith("Bearer "):
                token = token[len("Bearer "):]

        # Retrieve session ID from cookie
        session_id = self.session_manager.get_session_id_from_cookie(handler)
        
        if session_id and self.session_manager.is_session_valid(session_id):
            # Session is valid, return the associated user
            user_info = self.session_manager.get_user_from_session(session_id)
            return user_info['username'] if user_info else None

        if not token:
            logger.warning("No token provided")
            return None

        # Fetch the platform URL from the environment variable
        platform_url = os.getenv('MOVETODATA_API')

        if not platform_url:
            logger.error("MOVETODATA_API environment variable is not set")
            return None

        try:
            # Validate token with your platform
            headers = {
                'Accept-Language': 'en-US,en;q=0.5',
                'Authorization': f'Bearer {token}',
            }

            response = requests.get(f'{platform_url}/api/passport/users/me',
                                    headers=headers)

            # logger.info(f" token :  {token}")
            # logger.info(f" token : {response}")
            logger.info(f" token : {response.json()}")
            if response.status_code == 200:
                user_info = response.json()
                logger.info(f"User authenticated: {user_info['username']}")
                # Set session ID in cookie
                session_id = self.session_manager.create_session(user_info)
                self.session_manager.set_session_cookie(handler, session_id)
                return user_info['username']
            elif response.status_code == 401:
                logger.warning("Token is invalid or expired")
            else:
                logger.error(f"Error from platform API: {response.status_code}")
        except requests.exceptions.RequestException as e:
            logger.error(f"Error connecting to the platform API: {e}")

        return None


class CustomAuthorizer(Authorizer):
    def is_authorized(self, identity, handler: RequestHandler, *args, **kwargs):
        logger.info(f"Checking authorization for identity: {identity}")
        # Here you can enforce additional authorization policies if needed
        return identity is not None