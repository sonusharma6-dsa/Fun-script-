class Interpreter:
    def __init__(self, tree, data):
        self.tree = tree
        self.data = data

    def interpret(self):
        return self.evaluate(self.tree)

    def evaluate(self, node):
        if isinstance(node, dict):
            if node["type"] == "program":
                return self.execute_block(node["statements"])
            if node["type"] == "if":
                if self.evaluate(node["condition"]):
                    return self.execute_block(node["body"])
                return self.execute_block(node["else"])
            if node["type"] == "while":
                result = None
                while self.evaluate(node["condition"]):
                    result = self.execute_block(node["body"])
                return result
            if node["type"] == "for":
                result = None
                start = self.evaluate(node["start"])
                end = self.evaluate(node["end"])
                for value in range(start, end + 1):
                    self.data.write(node["variable"].value, value)
                    result = self.execute_block(node["body"])
                return result

        if not isinstance(node, list):
            if node.type == "INT":
                return int(node.value)
            if node.type == "FLT":
                return float(node.value)
            if node.type == "BOOL":
                return node.value == "true"
            if node.type == "VAR":
                return self.data.read(node.value)
            raise ValueError(f"Cannot evaluate token: {node.value!r}")

        if node[0] == "unary":
            value = self.evaluate(node[2])
            return -value if node[1] == "-" else not bool(value)

        op = node[1].value
        if op == "=":
            value = self.evaluate(node[2])
            self.data.write(node[0].value, value)
            return value

        left = self.evaluate(node[0])
        if op == "and" and not left:
            return False
        if op == "or" and left:
            return True
        right = self.evaluate(node[2])

        if op == "and":
            return bool(left) and bool(right)
        elif op == "or":
            return bool(left) or bool(right)
        elif op == "==":
            return left == right
        elif op == "!=":
            return left != right
        elif op == "<":
            return left < right
        elif op == "<=":
            return left <= right
        elif op == ">":
            return left > right
        elif op == ">=":
            return left >= right
        elif op == "+":
            return left + right
        elif op == "-":
            return left - right
        elif op == "*":
            return left * right
        elif op == "/":
            return left / right
        raise ValueError(f"Unknown operator: {op!r}")

    def execute_block(self, statements):
        result = None
        for statement in statements:
            result = self.evaluate(statement)
        return result