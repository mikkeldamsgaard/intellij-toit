interface I:
  a -> A

class A:
  b:

f i/I:
  d := i.a
  d.<caret>b

