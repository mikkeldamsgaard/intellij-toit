class A:
  i/int
  constructor --.i:

interface P:
  static a := A --<caret>i=1

f:
  P.a.i