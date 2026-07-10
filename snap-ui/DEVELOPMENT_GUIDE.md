# **GUIDELINES Frontend for MoveToData**

## Component Structure

- Component.view.tsx

  - Contains the viewing part of the component.
  - Contains all the states definition.
  - State manipulation will be done in this component

- Component.utils.ts

  - All the logical operations like transforming, manipulation etc will reside here. Take out only the logic and just separate it to the utils file.

- Component.module.scss

  - Read about BEM architecture / modules

- Component.api.ts

  - Separate all the api’s related to the app at one place.
  - Only write the api call inside this function. Don’t do transformation or manipulation of data here. Handle it via promise.
  - Naming convention of functions const functionNameAPI

- Component.constants.ts

  - Write all static things present under the component in this file. Follow the naming convention for constants.
  - Follow MACRO*CASE. Name : NAME*[type]
  - E.g : const HEADING_TEXT = getlanguageLabel(“heading”)
  - const OPERATORS_CONFIG = [“label” : “Hello”, “value” : “World”]

- A folder named components containing the sub components for the app / main component

This folder will contain the sub components for the app / component. If a portion of the app / component is heavy. Then you create a separate HEAVY component which will contain the same structure as the parent. Else it will simply be a ".view.tsx” file. And its respective constants and utils inside the parent folder.

### Example for separating logic and view

A function containing both logic and state manipulation. In that case, the state manipulation will be done inside the view file. And the write the manipulation into some functions inside

## Project Structure

- apps

  - dataset
  - kepler
  - ignite
  - editor
  - bezier
  - home
  - settings

- assets

  - icons
  - images
  - svg

- common

  - Components
    - Common components will be under this folder
  - Utils
    - AxiosInterceptors.ts
    - MoveToDataError.ts
    - index.ts
  - Api.ts
  - Constants.ts
  - Types.d.ts
  - common.scss

- layout

  - authentication
  - sidebar
  - header
  - app

- redux
  - reducers
  - constants
  - actions

- Routes.ts
- index.ts

## Naming Conventions

- Use below styling guide for typescript naming conventions, variable names etc
- https://google.github.io/styleguide/tsguide.html
- Use BEM architecture for SCSS : https://getbem.com/
- [App Name][Component] . [type] . extension
  - KeplerDashboardModal.view.ts
  - KeplerDashboardSubscribe.utils.ts
- Function names follow camelCase
- APIs name follow camelCase with API written as suffix. E.g camelCaseAPI
- Component names will be in PascalCase.
- Constants will be named via appending suffix defining the type of constants.
  Types present are TEXT, CONFIG
  E.g HEADING_TEXT, OPERATOR_CONFIG

## Use of utils

- Functions like date utils or any common functions should be created or used from utils.

## Language Labels

- When delivering a feature or component, always deliver the language model also

## Dark and Light Mode

- When delivering a feature or component, always test on dark and light mode

## Don’t Do

- Plus in variable joining for example VAR + “This is text” , use like “{VAR} This is text”
- Don’t add libraries without talking to another developer
- Follow the DRY coding method(Don’t repeat yourself) i.e instead of directly coding the new local states and api calls,look for already defined global states and utils, maybe you are re-doing or increasing the redundancy in the code.
- Don't use index as a key for components
- Don't use `map` function if it is not returning any value, use `forEach` instead.

# Error Handling

1.  Don’t wrap your code blocks with try catch for production code. This will be handled by ErrorBundary.
2.  Throw an instance of the MoveToDataError class at appropriate places.
3.  For API error handling

    1.  Return a promise from the api function and handle the rejection and resolve.
    2.  API returning any error code will be handled by default in axios interceptor and will respond with an error to the calling function.
        <br/>
        <br/>

    > MoveToDataError

    ```ts
    {
            code: 1xx | 2xx | 3xx | 4xx | 5xx.
            status: string <ErrorType | Heading>
            message: string message (limit to 20 words).
            fallback: ENUM < POPUP | ERROR_PAGE >
    }
    ```

4.  Supported Response codes:
    1. 200 OK
    2. 201 Created
    3. 203 Non-Authoritative Information
    4. 204 No Content
    5. 400 Bad Request
    6. 401 Unauthorized
    7. 403 Forbidden
    8. 404 Not Found
    9. 408 Request Timeout
    10. 500 Internal Server Error
    11. 501 Not Implemented
    12. 502 Bad Gateway
    13. 503 Service Unavailable
    14. 504 Gateway Timeout
