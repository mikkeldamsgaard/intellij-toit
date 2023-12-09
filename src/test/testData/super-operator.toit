class A:
  operator == other:

class B extends A:
  operator == other:
    return <caret>super other
