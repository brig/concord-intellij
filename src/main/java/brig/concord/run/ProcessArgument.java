package brig.concord.run;

import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Tag;
import org.jdom.Element;

import java.util.Objects;

@Tag("process-argument")
public class ProcessArgument implements JDOMExternalizable, Cloneable {
    private static final String NAME = "name";
    private static final String VALUE = "value";

    private String name;
    private String value;

    @Attribute("name")
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Attribute("value")
    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void readExternal(Element element) {
        this.name = element.getAttributeValue(NAME);
        this.value = element.getAttributeValue(VALUE);
    }

    public void writeExternal(Element element) {
        element.setAttribute(NAME, getName());
        element.setAttribute(VALUE, getValue());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProcessArgument that = (ProcessArgument) o;
        return Objects.equals(name, that.name) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value);
    }

    @Override
    public ProcessArgument clone() {
        try {
            return (ProcessArgument) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
