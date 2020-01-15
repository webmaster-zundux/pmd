/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.symbols.internal.impl;

import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.lang.java.symbols.JClassSymbol;
import net.sourceforge.pmd.lang.java.symbols.JConstructorSymbol;
import net.sourceforge.pmd.lang.java.symbols.JExecutableSymbol;
import net.sourceforge.pmd.lang.java.symbols.JFieldSymbol;
import net.sourceforge.pmd.lang.java.symbols.JFormalParamSymbol;
import net.sourceforge.pmd.lang.java.symbols.JMethodSymbol;
import net.sourceforge.pmd.lang.java.symbols.JTypeDeclSymbol;
import net.sourceforge.pmd.lang.java.symbols.JTypeParameterSymbol;

/**
 * Generic implementation for array symbols, which does not rely on
 * reflection.
 */
class ArraySymbolImpl implements JClassSymbol {

    private final JTypeDeclSymbol component;

    ArraySymbolImpl(@SuppressWarnings("PMD.UnusedFormalParameter") SymbolFactory<?> factory, JTypeDeclSymbol component) {
        this.component = Objects.requireNonNull(component, "Array symbol requires component");
        if (component instanceof JClassSymbol && ((JClassSymbol) component).isAnonymousClass()) {
            throw new IllegalArgumentException("Anonymous classes cannot be array components: " + component);
        }
    }

    @Override
    public @NonNull String getBinaryName() {
        if (component instanceof JClassSymbol) {
            return ((JClassSymbol) component).getBinaryName() + "[]";
        }
        return component.getSimpleName() + "[]";
    }

    @Override
    public String getCanonicalName() {
        if (component instanceof JClassSymbol) {
            String compName = ((JClassSymbol) component).getCanonicalName();
            return compName == null ? null : compName + "[]";
        }
        return component.getSimpleName() + "[]";
    }

    @Override
    public boolean isUnresolved() {
        return false;
    }

    @Override
    public @Nullable Class<?> getJvmRepr() {
        JTypeDeclSymbol elt = this.getArrayComponent();
        int depth = 0;
        while (elt instanceof JClassSymbol && ((JClassSymbol) elt).isArray()) {
            elt = ((JClassSymbol) elt).getArrayComponent();
            depth++;
        }

        Class<?> eltType = elt.getJvmRepr();
        if (eltType == null) {
            return null;
        }

        return Array.newInstance(eltType, (int[]) Array.newInstance(int.class, depth)).getClass();
    }

    @Override
    public @Nullable JExecutableSymbol getEnclosingMethod() {
        return null;
    }

    @Override
    public List<JMethodSymbol> getDeclaredMethods() {
        return Collections.singletonList(new ArrayCloneMethod(this));
    }

    @Override
    public List<JFieldSymbol> getDeclaredFields() {
        return Collections.singletonList(new ArrayLengthField(this));
    }

    @Override
    public @Nullable JClassSymbol getSuperclass() {
        return SymbolFactory.OBJECT_SYM;
    }

    @Override
    public List<JClassSymbol> getSuperInterfaces() {
        return SymbolFactory.ARRAY_SUPER_INTERFACES;
    }

    @Override
    public @NonNull JTypeDeclSymbol getArrayComponent() {
        return component;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ArraySymbolImpl that = (ArraySymbolImpl) o;
        return Objects.equals(component, that.component);
    }

    @Override
    public int hashCode() {
        return Objects.hash(component);
    }

    @Override
    public List<JClassSymbol> getDeclaredClasses() {
        return Collections.emptyList();
    }

    @Override
    public List<JConstructorSymbol> getConstructors() {
        return Collections.singletonList(new ArrayConstructor(this));
    }

    @Override
    @NonNull
    public String getPackageName() {
        return getArrayComponent().getPackageName();
    }

    @Override
    @NonNull
    public String getSimpleName() {
        return getArrayComponent().getSimpleName() + "[]";
    }

    @Override
    public int getModifiers() {
        int comp = getArrayComponent().getModifiers();
        return Modifier.FINAL | Modifier.ABSTRACT | (comp & ~Modifier.STATIC);
    }

    @Override
    public List<JTypeParameterSymbol> getTypeParameters() {
        return Collections.emptyList();
    }

    @Override
    @Nullable
    public JClassSymbol getEnclosingClass() {
        return null;
    }

    @Override
    public boolean isArray() {
        return true;
    }

    @Override
    public boolean isAnnotation() {
        return false;
    }

    @Override
    public boolean isPrimitive() {
        return false;
    }

    @Override
    public boolean isInterface() {
        return false;
    }

    @Override
    public boolean isEnum() {
        return false;
    }

    @Override
    public boolean isLocalClass() {
        return false;
    }

    @Override
    public boolean isAnonymousClass() {
        return false;
    }

    @Override
    public String toString() {
        return "array(" + component.toString() + ")";
    }

    private static class ArrayLengthField implements JFieldSymbol {

        private final JClassSymbol arraySymbol;

        ArrayLengthField(JClassSymbol arraySymbol) {
            this.arraySymbol = arraySymbol;
        }

        @Override
        public String getSimpleName() {
            return "length";
        }

        @Override
        public int getModifiers() {
            return Modifier.PUBLIC | Modifier.FINAL;
        }

        @Override
        public boolean isEnumConstant() {
            return false;
        }

        @Override
        public @NonNull JClassSymbol getEnclosingClass() {
            return arraySymbol;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ArrayLengthField that = (ArrayLengthField) o;
            return arraySymbol.equals(that.arraySymbol);
        }

        @Override
        public int hashCode() {
            return Objects.hash(arraySymbol);
        }
    }

    private static class ArrayCloneMethod implements JMethodSymbol {

        private final ArraySymbolImpl arraySymbol;

        ArrayCloneMethod(ArraySymbolImpl arraySymbol) {
            this.arraySymbol = arraySymbol;
        }

        @Override
        public String getSimpleName() {
            return "clone";
        }

        @Override
        public List<JFormalParamSymbol> getFormalParameters() {
            return Collections.emptyList();
        }

        @Override
        public boolean isVarargs() {
            return false;
        }

        @Override
        public int getArity() {
            return 0;
        }

        @Override
        public int getModifiers() {
            return Modifier.PUBLIC;
        }

        @Override
        public @NonNull JClassSymbol getEnclosingClass() {
            return arraySymbol;
        }

        @Override
        public List<JTypeParameterSymbol> getTypeParameters() {
            return Collections.emptyList();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ArrayCloneMethod that = (ArrayCloneMethod) o;
            return Objects.equals(arraySymbol, that.arraySymbol);
        }

        @Override
        public int hashCode() {
            return Objects.hash(arraySymbol);
        }
    }

    private static class ArrayConstructor implements JConstructorSymbol {

        private final ArraySymbolImpl arraySymbol;

        ArrayConstructor(ArraySymbolImpl arraySymbol) {
            this.arraySymbol = arraySymbol;
        }

        @Override
        public List<JFormalParamSymbol> getFormalParameters() {
            return Collections.singletonList(new JFormalParamSymbol() {

                @Override
                public JExecutableSymbol getDeclaringSymbol() {
                    return ArrayConstructor.this;
                }

                @Override
                public String getSimpleName() {
                    return "arg0";
                }

                @Override
                public boolean isFinal() {
                    return false;
                }

                // TODO equals/hashcode?
            });
        }

        @Override
        public boolean isVarargs() {
            return false;
        }

        @Override
        public int getArity() {
            return 1;
        }

        @Override
        public int getModifiers() {
            return Modifier.PUBLIC | Modifier.FINAL;
        }

        @Override
        public @NonNull JClassSymbol getEnclosingClass() {
            return arraySymbol;
        }

        @Override
        public List<JTypeParameterSymbol> getTypeParameters() {
            return Collections.emptyList();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ArrayConstructor that = (ArrayConstructor) o;
            return Objects.equals(arraySymbol, that.arraySymbol);
        }

        @Override
        public int hashCode() {
            return Objects.hash(arraySymbol);
        }
    }
}