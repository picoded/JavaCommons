# AccountTableAPI

Represents the API endpoint for AccountTable. This is automatically setup by default inside `CommonsPage`

## Configuration options
```
{
	"defaultAuthFilters" : "true"
}
```

## Keywords

The following is the key words, or parameter names and its respective meaning

+ accountID : account generated GUID
+ username : User login nice name
+ password : User login password
+ lockTimeout : The amount of seconds until user can try the next login attempt, in seconds.
+ sanatiseOutput : Defaults to true for all API endpoints, common escape characters are sanatised to HTML standard format.

## API

The following is the actual api endpoints

### login

When not provided any parameters, its use is to return the current login account status and information.

This return the following infromation including (but not limtied to) on succesful login
+ isLogin
+ usernameList

When login parameters, such as accountID, or username is provided, a login attempt will be done. Else a get request is assumed.

Note in event of a login failure, if a user was previously logged in, the session will be terminated.

### logout
Simple API endpoint with no parameters, logout any existingly logged in user. If present

### getInfo

### updateInfo

### new
Creates a new login user

Unless defaultAuthFilters is disabled, this 

### list

### resetPassword

## Deprecated notes
The following API endpoints has been deprecated from earlier version of javacommons

+ isLogin
+ lockTimeout
+ signup