class Lexer:
    digits = "0123456789"
    operations = "+-*/"

    def __init__(self, text):
        self.text = text
        self.idx = 0
        self.tokens = []
        self.char = self.text[0] if text else None

    def tokenize(self):
        while self.char is not None:
            if self.char in Lexer.digits:
                self.tokens.append(self.extract_number())
            elif self.char in Lexer.operations:
                self.tokens.append(Operation(self.char))
                self.move()
            elif self.char == " ":
                self.move()
            else:
                raise Exception(f"Illegal character: {self.char}")
        return self.tokens

    def extract_number(self):
        number = ""
        is_float = False
        while self.char is not None and (self.char in Lexer.digits or self.char == "."):
            if self.char == ".":
                is_float = True
            number += self.char
            self.move()
        return Float(number) if is_float else Integer(number)

    def move(self):
        self.idx += 1
        self.char = self.text[self.idx] if self.idx < len(self.text) else None


lexer = Lexer("12 + 3.5 * 4")

print(lexer.tokenize())