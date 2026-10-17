from lexer import Lexer
from parser import Parser
from interpreter import Interpreter
from data import Data

data = Data()
pending = ""

print("FunScript :")

while True:
    try:
        text = input()
    except (KeyboardInterrupt, EOFError):
        print("\nBye!")
        break

    if text.strip() == "exit":
        break

    pending = f"{pending} {text}".strip()
    if pending.count("{") != pending.count("}"):
        continue

    try:
        tokens = Lexer(pending).tokenize()
        tree = Parser(tokens).parse()
        result = Interpreter(tree, data).interpret()
        if result is not None:
            print(result)
        pending = ""
    except (ValueError, ZeroDivisionError) as error:
        print("Error:", error)
        pending = ""