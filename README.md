# FunScript

FunScript is a small programming language implemented in Python.

## Variables

Declare a variable with `make`, then use it in later expressions. Assignment
without `make` is also supported:

```text
make x = 10
x + 5
make y = x * 2
y
x = y + 1
```

## Expressions

Arithmetic operators use normal precedence: `*` and `/` before `+` and `-`.
Parentheses can be used to group expressions.

Comparison operators are `==`, `!=`, `<`, `<=`, `>`, and `>=`. Boolean
operators are `and`, `or`, and `not`. Boolean literals are `true` and `false`.

```text
x > 10 and not false
(x == 22) or (y < 3)
```

## Conditions

Blocks use braces and statements inside a block are separated by semicolons:

```text
if x > 10 {
    make result = 1;
    result
} else {
    make result = 0;
    result
}
```

`else if` is supported:

```text
if x < 0 { -1 } else if x == 0 { 0 } else { 1 }
```

## Loops

Use `while` for condition-controlled loops:

```text
make count = 0;
while count < 3 {
    make count = count + 1
}
```

Use `for` for an inclusive integer range:

```text
for i = 1 to 5 {
    i
}
```

The interactive shell accepts a complete statement on one line or collects
multiple lines until a braced block is complete. Type `exit` to quit.
