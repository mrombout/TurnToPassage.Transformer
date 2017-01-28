package nl.mikero.spiner.core.transformer.latex.model.command;

import nl.mikero.spiner.core.transformer.latex.model.LatexContainer;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A LaTeX environment.
 */
public class Environment extends BasicCommand {
    private final LatexContainer container;

    private final Command beginCommand;
    private final Command endCommand;

    /**
     * Constructs a new Environment.
     *
     * @param name environment name
     */
    public Environment(final String name) {
        super("begin");
        Objects.requireNonNull(name);

        this.container = new LatexContainer();

        this.beginCommand = new BasicCommand("begin").parameters().add(name).done();
        this.endCommand = new BasicCommand("end").parameters().add(name).done();

        parameters().add(name);
    }

    /**
     * Add a new {@link Command} to this environment.
     *
     * @param command command to add to environment
     * @return this
     */
    public Environment addCommand(final Command command) {
        Objects.requireNonNull(command);
        container.addCommand(command);

        return this;
    }

    public LatexContainer getContainer() {
        return container;
    }

    /**
     * Renders environment as a valid LaTeX environment string.
     *
     * @return this environment as a LaTex string
     */
    @Override
    public String toString() {
        List<String> commandStrings = container.getCommands().stream()
                .map(command -> "\t" + command.toString().replace("\n", "\n\t")).collect(Collectors.toList());
        String environmentContent = String.join("\n", commandStrings);

        return String.format("%s%n%s%n%s", beginCommand.toString(), environmentContent, endCommand.toString());
    }
}
