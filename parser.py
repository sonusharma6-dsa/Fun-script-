class Parser:
    def __init__(self,tokens):
        self.tokens = tokens
        self.idx = 0
        self.token = self.tokens[0] if self.tokens else None

    def factor(self):
        if self.token is not None and self.token.value in ("-", "not"):
            operation = self.token.value
            self.move()
            return ["unary", operation, self.factor()]
        if self.token is not None and self.token.type in ("INT", "FLT", "BOOL"):
            token = self.token
            self.move()
            return token
        elif self.token is not None and self.token.type == "VAR":
            token = self.token
            self.move()
            return token
        elif self.token is not None and self.token.value == "(":
            self.move()
            node = self.expression()
            if self.token is None or self.token.value != ")":
                raise ValueError("Expected ')'")
            self.move()
            return node
        if self.token is None:
            raise ValueError("Expected a number, found end of expression")
        raise ValueError(f"Expected a value, found {self.token.value!r}")

    def term(self):
        left_node = self.factor()
        while self.token is not None and (self.token.value == "*" or self.token.value == "/"):
            operation = self.token
            self.move()
            right_node = self.factor()
            output = [left_node,operation,right_node]
            left_node = output

        return left_node

    def parse(self):
        if self.token is None:
            raise ValueError("Expression cannot be empty")

        statements = [self.statement()]
        while self.token is not None:
            if self.token.value != ";":
                raise ValueError(f"Unexpected token: {self.token.value!r}")
            self.move()
            if self.token is not None:
                statements.append(self.statement())
        return statements[0] if len(statements) == 1 else {
            "type": "program", "statements": statements
        }

    def statement(self):
        if self.token.type == "DECL":
            self.move()
            variable = self.require_variable("Expected a variable name after 'make'")
            self.require_value("=", "Expected '=' after variable name")
            return [variable, self.previous, self.expression()]

        if self.token.type == "VAR" and self.token.value == "if":
            return self.condition_statement("if")
        if self.token.type == "VAR" and self.token.value == "while":
            return self.condition_statement("while")
        if self.token.type == "VAR" and self.token.value == "for":
            return self.for_statement()
        if self.token.type == "VAR" and self.peek_value() == "=":
            variable = self.token
            self.move()
            operation = self.token
            self.move()
            return [variable, operation, self.expression()]
        return self.expression()

    def condition_statement(self, kind):
        self.move()
        condition = self.expression_until("{")
        body = self.block()
        if kind == "while":
            return {"type": "while", "condition": condition, "body": body}
        alternate = []
        if self.token is not None and self.token.type == "VAR" and self.token.value == "else":
            self.move()
            if self.token is not None and self.token.type == "VAR" and self.token.value == "if":
                alternate = [self.condition_statement("if")]
            else:
                alternate = self.block()
        return {"type": "if", "condition": condition, "body": body, "else": alternate}

    def for_statement(self):
        self.move()
        variable = self.require_variable("Expected a variable name after 'for'")
        self.require_value("=", "Expected '=' after loop variable")
        start = self.expression_until("to")
        self.require_value("to", "Expected 'to' in for loop")
        end = self.expression_until("{")
        return {"type": "for", "variable": variable, "start": start,
                "end": end, "body": self.block()}

    def block(self):
        self.require_value("{", "Expected '{' before block")
        statements = []
        while self.token is not None and self.token.value != "}":
            statements.append(self.statement())
            if self.token is not None and self.token.value == ";":
                self.move()
            elif self.token is not None and self.token.value != "}":
                raise ValueError("Expected ';' or '}' between statements")
        self.require_value("}", "Expected '}' after block")
        return statements

    def expression_until(self, value):
        start = self.idx
        depth = 0
        while self.token is not None:
            if self.token.value == "(":
                depth += 1
            elif self.token.value == ")":
                depth -= 1
            if depth == 0 and self.token.value == value:
                if self.idx == start:
                    raise ValueError("Expected an expression")
                tokens = self.tokens[start:self.idx]
                self.token = self.tokens[self.idx]
                return Parser(tokens).parse()
            self.move()
        raise ValueError(f"Expected {value!r}")

    def require_variable(self, message):
        if self.token is None or self.token.type != "VAR":
            raise ValueError(message)
        token = self.token
        self.move()
        return token

    def require_value(self, value, message):
        if self.token is None or self.token.value != value:
            raise ValueError(message)
        token = self.token
        self.move()
        self.previous = token
        return token

    def peek_value(self):
        if self.idx + 1 >= len(self.tokens):
            return None
        return self.tokens[self.idx + 1].value

    def expression(self):
        left_node = self.boolean_term()
        while self.token is not None and self.token.value == "or":
            operation = self.token
            self.move()
            left_node = [left_node, operation, self.boolean_term()]
        return left_node

    def boolean_term(self):
        left_node = self.comparison()
        while self.token is not None and self.token.value == "and":
            operation = self.token
            self.move()
            left_node = [left_node, operation, self.comparison()]
        return left_node

    def comparison(self):
        left_node = self.arithmetic()
        while self.token is not None and self.token.value in (
            "==", "!=", "<", "<=", ">", ">="
        ):
            operation = self.token
            self.move()
            left_node = [left_node, operation, self.arithmetic()]
        return left_node

    def arithmetic(self):
        left_node = self.term()
        while self.token is not None and self.token.value in ("+", "-"):
            operation = self.token
            self.move()
            left_node = [left_node, operation, self.term()]
        return left_node

    def term(self):
        left_node = self.factor()
        while self.token is not None and self.token.value in ("*", "/"):
            operation = self.token
            self.move()
            right_node = self.factor()
            left_node = [left_node, operation, right_node]
        return left_node

    def move(self):
        self.idx += 1
        self.token = self.tokens[self.idx] if self.idx < len(self.tokens) else None
