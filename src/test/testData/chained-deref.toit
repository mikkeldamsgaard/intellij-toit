class A:
  b -> B: return B

class B:
  a -> A: return A

main:
  (A).b.a.b.a.<caret>b