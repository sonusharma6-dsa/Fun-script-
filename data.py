class Data:
    def __init__(self):
        self.variables = {}

    def read(self, name):
        if name not in self.variables:
            raise ValueError(f"Undefined variable: {name}")
        return self.variables[name]

    def write(self, name, value):
        self.variables[name] = value