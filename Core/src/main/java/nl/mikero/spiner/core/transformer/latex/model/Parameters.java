package nl.mikero.spiner.core.transformer.latex.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import nl.mikero.spiner.core.transformer.latex.model.command.BasicCommand;

/**
 * Collection of {@link Parameter}.
 */
public class Parameters {
    private final BasicCommand parent;
    private final List<Parameter> parameters;

    /**
     * Constructs a new Parameters.
     *
     * @param parent parent command
     */
    public Parameters(final BasicCommand parent) {
        this.parent = Objects.requireNonNull(parent);
        this.parameters = new ArrayList<>();
    }

    /**
     * Adds a new {@link Parameter} to this collection.
     *
     * @param value parameter to add
     * @return this
     */
    public final Parameters add(final String value) {
        this.parameters.add(new Parameter(value));

        return this;
    }

    /**
     * Returns parent.
     *
     * Usage: `new BasicCommand().parameters().add("param1").add("param2").build().options().add("option1").build()`.
     *
     * @return parent
     */
    public final BasicCommand build() {
        return parent;
    }

    /**
     * Renders this collection to a valid LaTeX string.
     *
     * @return latex representation of parameter collection
     */
    @Override
    public final String toString() {
        StringBuilder sb = new StringBuilder();

        for(Parameter parameter : parameters) {
            sb.append(parameter.toString());
        }

        return sb.toString();
    }
}
