class Parser:
    def __init__(self,tokens):
        self.tokens = tokens
        self.idx = 0
        self.token = self.tokens[self.idx]

    def factor(self):
        if self.token is not None and (self.token.type == "INT" or self.token.type == "FLT"):
            return self.token

    def term(self):
        left_node  = self.factor()
        self.move()
        output = left_node
        while self.token is not None and (self.token.value == "*" or self.token.value == "/"):
            operation = self.token
            self.move()
            right_node = self.factor()
            self.move()
            output = [left_node,operation,right_node]

        return output

    def parse(self):
        return self.expression()

    def expression(self):
        left_node = self.term()
        output = left_node
        while self.token is not None and (self.token.value == "+" or self.token.value == "-"):
            operation = self.token
            self.move()
            right_node = self.term()
            output = [left_node,operation,right_node]

        return self.term()
    
    def move(self):
            self.idx += 1
            self.token = self.tokens[self.idx] if self.idx < len(self.tokens) else None

pas
    
    
    
