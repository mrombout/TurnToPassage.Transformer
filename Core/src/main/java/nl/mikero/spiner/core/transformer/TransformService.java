package nl.mikero.spiner.core.transformer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

import com.google.inject.Inject;
import nl.mikero.spiner.core.exception.TwineTransformationFailedException;
import nl.mikero.spiner.core.inject.PublishedRepairer;
import nl.mikero.spiner.core.twine.TwineArchiveParser;
import nl.mikero.spiner.core.twine.TwineRepairer;
import nl.mikero.spiner.core.twine.extended.ExtendTwineXmlTransformer;
import nl.mikero.spiner.core.twine.model.TwStoriesdata;
import nl.mikero.spiner.core.twine.model.TwStorydata;
import org.apache.commons.io.IOUtils;

/**
 * Transforms an {@link InputStream} into a different format using a {@link Transformer} and outputs it to an
 * {@link OutputStream}.
 */
public class TransformService {
    private final TwineRepairer publishedRepairer;
    private final ExtendTwineXmlTransformer extendTwineXmlTransformer;
    private final TwineArchiveParser twineArchiveParser;

    /**
     * Constructs a new TransformService.
     *
     * @param publishedRepairer repairer to use for published twine stories
     * @param extendTwineXmlTransformer twine extended to use
     * @param twineArchiveParser twine archive parser to use
     */
    @Inject
    public TransformService(@PublishedRepairer final TwineRepairer publishedRepairer,
                            final ExtendTwineXmlTransformer extendTwineXmlTransformer,
                            final TwineArchiveParser twineArchiveParser) {
        this.publishedRepairer = publishedRepairer;
        this.extendTwineXmlTransformer = extendTwineXmlTransformer;
        this.twineArchiveParser = twineArchiveParser;
    }

    /**
     * Transforms a published Twine story or Twine archive into another format.
     *
     * @param inputStream input stream to a published Twine story or archive
     * @param outputStream output stream to write the transformed input to
     * @param transformer transformer to use
     * @throws TwineTransformationFailedException when transformation fails
     */
    public void transform(final InputStream inputStream,
                          final OutputStream outputStream,
                          final Transformer transformer) {
        Objects.requireNonNull(inputStream);
        Objects.requireNonNull(outputStream);
        Objects.requireNonNull(transformer);

        try (ByteArrayOutputStream repairedInput = new ByteArrayOutputStream();
             ByteArrayOutputStream transformedInput = new ByteArrayOutputStream()) {
            repair(inputStream, repairedInput);
            extend(repairedInput, transformedInput);

            TwStoriesdata stories = parse(transformedInput);
            for (TwStorydata twStorydata : stories.getTwStorydata()) {
                transformer.transform(twStorydata, outputStream);
            }
        } catch (Exception e) {
            throw new TwineTransformationFailedException("Could not transform input.", e);
        }
    }

    /**
     * Reads input, repairs and writes to output.
     *
     * @param inputStream twine xml input
     * @param outputStream repaired twine xml output
     * @throws IOException if repair fails
     * @see nl.mikero.spiner.core.twine.TwineArchiveRepairer
     */
    private void repair(final InputStream inputStream, final OutputStream outputStream) throws IOException {
        try (InputStream in = new ByteArrayInputStream(IOUtils.toByteArray(inputStream))) {
            publishedRepairer.repair(in, outputStream);
        }
    }

    /**
     * Reads input, extends and writes to output.
     *
     * @param repairedOutput repaired twine xml input
     * @param transformedOutput extended twine xml output
     * @throws IOException if extend fails
     */
    private void extend(
            final ByteArrayOutputStream repairedOutput,
            final OutputStream transformedOutput) throws IOException {
        try(InputStream in = new ByteArrayInputStream(repairedOutput.toByteArray())) {
            extendTwineXmlTransformer.transform(in, transformedOutput);
        }
    }

    /**
     * Reads input, parses and returns TwStoriesdata.
     *
     * @param transformedInput twine xml input
     * @return TwStoriesdata based on xml input
     * @throws IOException if parse fails
     */
    private TwStoriesdata parse(final ByteArrayOutputStream transformedInput) throws IOException {
        try (InputStream in = new ByteArrayInputStream(transformedInput.toByteArray())) {
            return twineArchiveParser.parse(in);
        }
    }
}
