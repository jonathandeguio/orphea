# Authorization Based Endpoints

Whenever creating any new endpoint make sure you annotate it with `@PreAuthorize`.

> Authorization support 4 permissions for authorization based endpoints
>
> 1.  `@PreAuthorize(Auth.VIEWER)`
> 2.  `@PreAuthorize(Auth.EDITOR)`
> 3.  `@PreAuthorize(Auth.OWNER)`
> 4.  `@PreAuthorize(Auth.PLATFORM_ADMIN)`
>
> ---

NOTE: Auth.VIEWER is a simple static string "isViewer(#id)". It is used to make the permission writing simple

---

### These permission checker functions will by default pick any value defined as id in the function parameters or function variable and check the permission based on that.

You can also mark some other variable as Id using @Param annotation in the parameters to be used by PreAuthorize. (Check example)

<i><b>Example</b></i>

```java
// 1. Using PathVariable
@PathVariable("pathVariable") UUID id;

// 2. Using RequestPara
@RequestParam("requestParam") UUID id;

// 3. Using value inside request body
@GetMapping("/api/myEndpoint")
@PreAuthorize(Auth.VIEWER)
ResponseEntity<Object> myEndPoint(Principal principal, @RequestBody MyRequest myRequest) {
 UUID id = myRequest.getMyId(); // @PreAuthorize will automatically pick this
}

// 4. Using Param annotation
@GetMapping("/api/myFunPoint/{datasetId}")
@PreAuthorize(Auth.VIEWER)
ResponseEntity<Object> myFunPoint(Principal principal, @Param("id") @PathVariable("datasetId") UUID datasetId) {
 // @PreAuthorize will automatically pick datsetId for authorization check using datasetId due to @Param annotation
 // Be sure to mark the @Param("id") as id only else it will not work
}
```

---

### If your use case does not fall into above mentioned cases you can also choose to use the dot(.) based permission access using SpEL (Spring Expression Language)

```java
// Using SpEL
@GetMapping("/api/myEndpoint")
@PreAuthorize("isViewer(myRequest.myId)")
ResponseEntity<Object> myEndPoint(Principal principal, @RequestBody MyRequest myRequest) {
}
```

---

### You can add more permissions in the `src/main/java/io/movetodata/passport/security/CustomMethodSecurityExpressionRoot.java`

```java
public boolean newPermission(UUID resourceId, AuthRole role) {
    UserPrincipal authUser = (UserPrincipal) authentication.getPrincipal();
    UUID userId = authUser.getId();

    return true; // WRITE YOUR LOGIC
}
```

After this you can add its SpEL to the `src/main/java/io/movetodata/passport/library/Auth.java` file or use directly.

<i><b>Example</b></i>

```java
// After adding the SpEL string to the Auth file

public static final String MY_PERMISSION = "myPermission(#id) or myPermission(@id)";

@PreAuthorize(Auth.MY_PERMISSION)

// Directly using SpEL
@PreAuthorize("myPermission(id)")
```
