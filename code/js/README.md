
# Login
|            | edit | submit     | error    | sucess   |
|------------|------|------------|----------|----------|
| editting   | -    | submitting | editting | -        |
| submitting | -    | -          | editting | redirect |
| redirect   | -    | -          | -        | -        |


```js
type State =
  | { tag: 'editing'; error?: string; inputs: { username: string; password: string } }
  | { tag: 'submitting'; username: string }
  | { tag: 'redirect' };

type Action =
  | { type: 'edit'; inputName: string; inputValue: string }
  | { type: 'submit' }
  | { type: 'error'; message: string }
  | { type: 'success' };
  
  ```

  # Register
|            | edit | submit     | error    | sucess   |
|------------|------|------------|----------|----------|
| editting   | edit | submitting | editting | -        |
| submitting | -    | -          | editting | redirect |
| redirect   | -    | -          | -        | -        |

```js
type State =
  | { tag: 'editing'; error?: string; inputs: { username: string; email:string, confirmpassword:string, password: string } }
  | { tag: 'submitting'; username: stringm, email:string }
  | { tag: 'redirect' };

type Action =
    | { type: 'edit'; inputName: string; inputPassword: string, inputEmail:string, inputConfirmPassword:string  }
    | { type: 'submit' }
    | { type: 'error'; message: string }
    | { type: 'success' };
    
```
    
# Home
|          | login |  register    | users-stats | systemInfo | user-by-id |
|----------|-------|--------------|-------------|------------|------------|
| idle     |  -    |      -       |      -      |     -      |     -      |
| redirect |  -    |      -       |      -      |     -      |     -      |

```js
type State =
  | { tag: 'idle' }
  | { tag: 'redirect' };
  
type Action =
    | { type: 'login' }
    | { type: 'register' }
    | { type: 'users-stats' }
    | { type: 'systemInfo' }
    | { type: 'user-by-id' };
    
```
    
# Users Stats
|          |   | user-stats | navigate-to-page | refresh | success | error |
|----------|---|------------|------------------|---------|---------|-------|
| loading  | - | -          |                  | -       | loaded  |       |
| loaded   | - | loading    | loading          | loading | -       |       |
| redirect | - | -          | -                | -       | -       |       |

```js
type State =
  | { tag: 'loading' }
  | { tag: 'loaded' }
  | { tag: 'redirect' };

type Action =
    | { type: 'user-stats' }
    | { type: 'navigate-to-page' }
    | { type: 'refresh' }
    | { type: 'success' }
    | { type: 'error' };
```

# System Info
|          | home     | logout  | users-stats | success  | error    | 
|----------|----------|---------|-------------|----------|----------|
| loading  | -        |         | -           | redirect | redirect |
| loaded   | redirect | loading | -           |          |          |
| redirect | -        | -       | -           |          |          |




```js
type State =
  | { tag: 'loading' }
  | { tag: 'loaded' }
  | { tag: 'redirect' };

type Action =
    | { type: 'home' }
    | { type: 'logout' }
    | { type: 'users-stats' };
```


