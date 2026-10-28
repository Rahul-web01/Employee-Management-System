# Route and Payload Documentation

This application renders HTML on the server (Thymeleaf), so its routes return web pages and redirects rather than JSON. Base URL for local use: `http://localhost:8080`.

## Routes

| Method | URL | Description | Success response |
|---|---|---|---|
| GET | `/` | Home | Redirects to `/employees` |
| GET | `/employees` | Employee list. Optional query `keyword` searches first and last name (case-insensitive). | 200, `employees/list` page |
| GET | `/employees/new` | Empty add form | 200, `employees/form` page |
| GET | `/employees/edit/{id}` | Edit form filled with the employee | 200, `employees/form` page |
| POST | `/employees/save` | Create (no `id`) or update (with `id`) | 302 redirect to `/employees` |
| POST | `/employees/delete/{id}` | Delete an employee | 302 redirect to `/employees` |

## Request: `POST /employees/save`

Content type: `application/x-www-form-urlencoded` (sent by the HTML form).

| Field | Type | Required | Rules |
|---|---|:---:|---|
| `id` | number | No | Empty for a new employee; set when editing |
| `firstName` | text | Yes | 2 to 50 characters |
| `lastName` | text | Yes | 2 to 50 characters |
| `email` | text | Yes | Valid email, max 100 characters, must be unique |
| `designation` | text | No | Max 100 characters |
| `salary` | decimal | Yes | 0 or more, up to 10 digits and 2 decimals |
| `hireDate` | date (`yyyy-MM-dd`) | No | Today or earlier |
| `departmentId` | number | Yes | Id of an existing department |

## Responses to bad input

| Situation | Response |
|---|---|
| A field breaks a rule | 200, the form page again with a red message under each wrong field. Typed values are kept. |
| Email already used by another employee | 200, the form page again with a message under the email field |
| No department selected | 200, the form page again with a message under the department field |

## Error pages

Errors are handled in one place (`GlobalExceptionHandler`) and shown on `error/error`.

| HTTP status | When it happens |
|---|---|
| 400 | A URL value has the wrong type, for example `/employees/edit/abc` |
| 404 | Employee or department id not found, or the page does not exist |
| 409 | A duplicate or linked record blocks the action |
| 500 | Any unexpected error. The user sees a generic message; details go to the server log only. |

## Data model

**Department**: `id`, `name` (unique), `location`

**Employee**: `id`, `firstName`, `lastName`, `email` (unique), `designation`, `salary`, `hireDate`, `department` (many employees to one department)
