import polyglot
class PythonClass1:
    def process_data(self):
        arr = polyglot.import_value('arr')
        return [i * 2 for i in arr]
instance1 = PythonClass1()