import encoding.yaml

input ::= """
cars:
 - &bmw
   color: red
   year: 2020
   engine-type: diesel
 - &vw
   color: blue
   year: 2022
   engine-type: electric
madame:
  name: Jane
  car: *vw
mister:
  name: John
  car: *bmw
    """

main:
  print (yaml.parse input)