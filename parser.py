class Parser:
    def __init__(self,tokens):
        self.tokens = tokens
        self.idx = 0
        self.token = self.tokens[0] if self.tokens else None

    def factor(self):
        if self.token is not None and (self.token.type == "INT" or self.token.type == "FLT"):
            token = self.token
            self.move()
            return token
        elif self.token is not None and (self.token.type == "INT" or self.token.type == "FLT")
        if self.token is None:
            raise ValueError("Expected a number, found end of expression")
        raise ValueError(f"Expected a number, found {self.token.value!r}")

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
        tree = self.expression()
        if self.token is not None:
            raise ValueError(f"Unexpected token: {self.token.value!r}")
        return tree

    def expression(self):
        left_node = self.term()
        while self.token is not None and (self.token.value == "+" or self.token.value == "-"):
            operation = self.token
            self.move()
            right_node = self.term()
            left_node = [left_node, operation, right_node]

        return left_node
    
    def move(self):
        self.idx += 1
        self.token = self.tokens[self.idx] if self.idx < len(self.tokens) else None
