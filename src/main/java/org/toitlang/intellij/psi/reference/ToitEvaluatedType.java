package org.toitlang.intellij.psi.reference;

import com.intellij.psi.PsiElement;
import lombok.*;
import org.toitlang.intellij.psi.ToitFile;
import org.toitlang.intellij.psi.ast.*;
import org.toitlang.intellij.psi.expression.ToitExpressionVisitor;
import org.toitlang.intellij.psi.scope.ToitScope;

import java.util.*;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class ToitEvaluatedType {
  private final ToitFile file;
  private final ToitStructure structure;
  private final boolean isStatic;
  private final boolean estimated;
  public final static ToitEvaluatedType UNRESOLVED = new ToitEvaluatedType(null, null, false, false);

  public static ToitEvaluatedType staticStructure(ToitStructure structure) {
    return new ToitEvaluatedType(null, structure, true, false);
  }

  public static ToitEvaluatedType variableStructure(ToitStructure structure) {
    return new ToitEvaluatedType(null, structure, false, false);
  }

  public static ToitEvaluatedType fromType(ToitType type) {
    if (type == null || Objects.equals(type.getName(), "any")) {
      return null;
    }
    ToitStructure toitStructure = type.resolve();
    if (toitStructure == null) {
      return null;
    }
    return ToitEvaluatedType.variableStructure(toitStructure);
  }

  public static ToitEvaluatedType file(ToitFile file) {
    return new ToitEvaluatedType(file, null, false, false);
  }

  @Override
  public String toString() {
    if (file != null) return "module " + file.getVirtualFile().getNameWithoutExtension();
    if (structure != null) return structure.getName();
    return "any";
  }

  public ToitEvaluatedType nonStatic() {
    return new ToitEvaluatedType(file, structure, false, estimated);
  }

  public boolean isUnresolved() {
    return file == null && structure == null;
  }


  private final static String LIST_CLASS_NAME = "List";
  private final static String BYTE_ARRAY_CLASS_NAME = "ByteArray";
  private final static String INT_CLASS_NAME = "int";
  private final static String FLOAT_CLASS_NAME = "float";
  private final static String BOOL_CLASS_NAME = "bool";
  private final static String STRING_CLASS_NAME = "string";
  private final static String MAP_CLASS_NAME = "Map";
  private final static String SET_CLASS_NAME = "Set";

  private final static String SUPER = "super";
  private final static String THIS = "this";
  private final static String IT = "it";
  private final static String PRIMITIVE = "#primitive";


  public static ToitEvaluatedType evaluate(ToitExpression expression, ToitScope scope) {
    return expression.accept(new ToitExpressionVisitor<>() {
      private List<ToitEvaluatedType> recurse(ToitExpression expression) {
        return expression.acceptChildren(this);
      }

      private ToitEvaluatedType singularRecurse(ToitExpression expression) {
        var recursed = recurse(expression);
        if (recursed.isEmpty()) return UNRESOLVED;
        if (recursed.size() > 1) {
          System.err.println("Multiple recurses::: " + recursed);
        }
        var result = recursed.get(recursed.size() - 1);
        if (result == null) return UNRESOLVED;
        return result;
      }

      private ToitEvaluatedType resolveToStruct(ToitScope scope, String name) {
        List<ToitReferenceTarget> resolved = scope.resolve(name);
        for (PsiElement psiElement : resolved) {
          if (psiElement instanceof ToitStructure)
            return new ToitEvaluatedType(null, (ToitStructure) psiElement, false, false);
        }
        return UNRESOLVED;
      }

      @Override
      public ToitEvaluatedType visit(ToitPostfixExpression toitPostfixExpression) {
        var last = toitPostfixExpression.getChildrenOfType(ToitExpression.class).stream().reduce((f,s)->s).orElse(null);
        if (last == null) return null;
        var reference = last.getReference();
        if (reference == null) return null;
        for (ToitReferenceTarget target : ((ToitReference) reference).getDestinations()) {
          var evaluatedType = target.getEvaluatedType();
          if (evaluatedType != null) return evaluatedType;
        }
        return null;
      }

      @Override
      public ToitEvaluatedType visit(ToitPrimaryExpression toitPrimaryExpression) {
        var referenceIdentifiers = toitPrimaryExpression.getChildrenOfType(ToitReferenceIdentifier.class);
        if (referenceIdentifiers.isEmpty()) {
          return singularRecurse(toitPrimaryExpression).nonStatic();
        }

        var toitReferenceIdentifier = referenceIdentifiers.get(0);
        var reference = toitReferenceIdentifier.getReference();
        for (ToitReferenceTarget target : reference.getDestinations()) {
          var evaluatedType = target.getEvaluatedType();
          if (evaluatedType != null) return evaluatedType;
        }
        return null;
      }

      @Override
      public ToitEvaluatedType visit(ToitTopLevelExpression toitTopLevelExpression) {
        var recursed = recurse(toitTopLevelExpression);
        if (recursed.isEmpty()) return UNRESOLVED;
        return recursed.get(recursed.size() - 1); // Return the value of the last expression.
      }

      @Override
      public ToitEvaluatedType visit(ToitAssignmentExpression toitAssignmentExpression) {
        return evaluate((ToitExpression) toitAssignmentExpression.getLastChild(), scope);
      }

      @Override
      public ToitEvaluatedType visit(ToitCallExpression toitCallExpression) {
        var expressions = toitCallExpression.getChildrenOfType(ToitExpression.class);
        if (expressions.isEmpty()) return ToitEvaluatedType.UNRESOLVED;
        return expressions.get(0).getType(scope);
      }

      @Override
      public ToitEvaluatedType visit(ToitListLiteral toitListLiteral) {
        if (toitListLiteral.isByteArray()) {
          return resolveToStruct(scope, BYTE_ARRAY_CLASS_NAME);
        } else
          return resolveToStruct(scope, LIST_CLASS_NAME);
      }

      @Override
      public ToitEvaluatedType visit(ToitSetLiteral toitSetLiteral) {
        return resolveToStruct(scope, SET_CLASS_NAME);
      }

      @Override
      public ToitEvaluatedType visit(ToitMapLiteral toitMapLiteral) {
        return resolveToStruct(scope, MAP_CLASS_NAME);
      }

      @Override
      public ToitEvaluatedType visit(ToitSimpleLiteral toitSimpleLiteral) {
        if (toitSimpleLiteral.isString()) {
          return resolveToStruct(scope, STRING_CLASS_NAME);
        } else if (toitSimpleLiteral.isInt()) {
          return resolveToStruct(scope, INT_CLASS_NAME);
        } else if (toitSimpleLiteral.isFloat()) {
          return resolveToStruct(scope, FLOAT_CLASS_NAME);
        } else if (toitSimpleLiteral.isBoolean()) {
          return resolveToStruct(scope, BOOL_CLASS_NAME);
        }
        return UNRESOLVED;
      }

      @Override
      public ToitEvaluatedType visit(ToitRelationalExpression toitRelationalExpression) {
        if (toitRelationalExpression.isAs()) {
          var subExpressions = toitRelationalExpression.getChildrenOfType(ToitExpression.class);
          if (subExpressions.isEmpty()) return UNRESOLVED;
          var evaluatedType = subExpressions.get(subExpressions.size() - 1).accept(this);
          if (evaluatedType.getStructure() != null) return evaluatedType.nonStatic();
          return evaluatedType;
        }
        return resolveToStruct(scope, BOOL_CLASS_NAME);
      }

      @Override
      public ToitEvaluatedType visit(ToitShiftExpression toitShiftExpression) {
        return resolveToStruct(scope, INT_CLASS_NAME);
      }

      @Override
      public ToitEvaluatedType visit(ToitUnaryExpression toitUnaryExpression) {
        return singularRecurse(toitUnaryExpression);
      }

      @Override
      public ToitEvaluatedType visitExpression(ToitExpression expression) {
        return UNRESOLVED;
      }


      // Todo: Binary expression
    });
  }

  public PsiElement getPsiElement() {
    if (structure != null) return structure;
    if (file != null) return file;
    throw new RuntimeException("Called getPsiElement on unresolved type");
  }

  public boolean isAssignableTo(ToitEvaluatedType variableType) {
    if (variableType.isUnresolved()) return true;
    if (isUnresolved()) return true; // TODO: Fix this

    if (structure == null || variableType.structure == null) return false;
    return structure.isAssignableTo(variableType.structure);
  }

  public boolean isAssignableTo(ToitStructure structure) {
    if (isUnresolved()) return true; // TODO: Fix this

    if (this.structure == null) return false;
    return this.structure.isAssignableTo(structure);
  }
}
