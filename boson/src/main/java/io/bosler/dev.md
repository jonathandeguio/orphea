# Refactoring 101

1. Never import a JPA repository someplace other than its service.
2. Keep in mind that you don't need whole User object 99% of the times refactor the code and method arguments to accept UUID instead of a user object.
3. Use `@AuthenticationPrincipal AuthUser authUser` in place of principal.
4. There should always be a single exit point (return) in a method. (-.-)
5. Do not use string concatenation, use `Messageformat.format` method.
6. Controller should not have any business logic.
7. Make sure you are not autowiring `authzService`.
8. Make your own exceptions and handle them in the `GlobalExceptionHandler`
9. Never return a Response Body with erroneous code. Always throw exception instead.
10. Use streams and FunctionalInterfaces.
11. Make sure each controller is using PreAuthorize annotation