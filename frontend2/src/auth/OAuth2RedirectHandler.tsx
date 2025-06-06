import { useEffect } from 'react';

import { BOSLER_TOKEN } from './constants';
import { Navigate, useSearchParams } from 'react-router-dom';

const OAuth2RedirectHandler = (props : any) => {
    const [searchParams] = useSearchParams();

    const token = searchParams.get('token');

    useEffect(() => {
        
        
        if(token) {
            localStorage.setItem(BOSLER_TOKEN, token);
        }
    }, [token])

    // FIX THIS
    return (        
        token ? <Navigate to="/"/> : <Navigate to="/login"/> 
    )
}
export default OAuth2RedirectHandler;