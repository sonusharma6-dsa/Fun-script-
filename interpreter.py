class Interpreter:
    def __init__(self, tree):
        self.tree = tree

    def interpret(self):
        return self.evaluate(self.tree)

    def evaluate(self, node):
        
        if not isinstance(node, list):
            if node.type == "INT":
                return int(node.value)
            return float(node.value)

        
        left = self.evaluate(node[0])
        op = node[1].value
        right = self.evaluate(node[2])

        if op == "+":
            return left + right
        elif op == "-":
            return left - right
        elif op == "*":
            return left * right
        elif op == "/":
            return left / right