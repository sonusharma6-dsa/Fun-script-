from tokens import Float, Integer, Operation , Paren , Declaration , Variable



class Lexer:
    digits = "0123456789"
    operations = "+-*/="
    words = "abcdefghijklmnopqrstuvwxyz"
    declarations = ["make"]
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

            elif self.char.isspace():
                self.move()

            elif self.char in "()":
                self.tokens.append(Paren(self.char))
                self.move()

            elif self.char in Lexer.words:
                word = self.extract_words()

                if word in Lexer.declarations:
                    self.tokens.append(Declaration(word))

                else:
                    self.tokens.append(Variable(word))

            else:
                raise ValueError(f"Illegal character: {self.char}")
        return self.tokens
    def extract_words(self):
        word = ""
        while self.char is not None and self.char in Lexer.words:
            word += self.char
            self.move()
        return word

    def extract_number(self):
        number = ""
        decimal_count = 0
        while self.char is not None and (self.char in Lexer.digits or self.char == "."):
            if self.char == ".":
                decimal_count += 1
                if decimal_count > 1:
                    raise ValueError(f"Invalid number: {number + self.char}")
            number += self.char
            self.move()
        if number == ".":
            raise ValueError("Invalid number: .")
        return Float(number) if decimal_count else Integer(number)

    def move(self):
        self.idx += 1
        self.char = self.text[self.idx] if self.idx < len(self.text) else None
