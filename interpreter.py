class Interpreter:
    def __init__(self, tree, data):
        self.tree = tree
        self.data = data

    def interpret(self):
        return self.evaluate(self.tree)

    def evaluate(self, node):
        if not isinstance(node, list):
            if node.type == "INT":
                return int(node.value)
            if node.type == "FLT":
                return float(node.value)
            if node.type == "VAR":
                return self.data.read(node.value)
            raise ValueError(f"Cannot evaluate token: {node.value!r}")

        op = node[1].value

        if op == "=":
            value = self.evaluate(node[2])
            self.data.write(node[0].value, value)
            return value

        left = self.evaluate(node[0])
        right = self.evaluate(node[2])

        if op == "+":
            return left + right
        elif op == "-":
            return left - right
        elif op == "*":
            return left * right
        elif op == "/":
            return left / right
        raise ValueError(f"Unknown operator: {op!r}")