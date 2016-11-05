package nl.mikero.spiner.core.transformer;

import com.google.inject.Inject;
import nl.mikero.spiner.core.inject.PublishedRepairer;
import nl.mikero.spiner.core.twine.TwineArchiveParser;
import nl.mikero.spiner.core.twine.TwineRepairer;
import nl.mikero.spiner.core.twine.extended.ExtendTwineXmlTransformer;
import nl.mikero.spiner.core.twine.model.TwStoriesdata;
import nl.mikero.spiner.core.twine.model.TwStorydata;
import org.apache.commons.io.IOUtils;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.util.Objects;

public class TransformService {
    private final TwineRepairer publishedRepairer;
    private final ExtendTwineXmlTransformer extendTwineXmlTransformer;
    private final TwineArchiveParser twineArchiveParser;

    @Inject
    public TransformService(@PublishedRepairer TwineRepairer publishedRepairer, ExtendTwineXmlTransformer extendTwineXmlTransformer, TwineArchiveParser twineArchiveParser) {
        this.publishedRepairer = publishedRepairer;
        this.extendTwineXmlTransformer = extendTwineXmlTransformer;
        this.twineArchiveParser = twineArchiveParser;
    }

    /**
     * Transforms a published Twine story or Twine archive into another format.
     *
     * @param inputStream input stream to a published Twine story or archive
     * @param outputStream output stream to write the transformed input to
     */
    public void transform(InputStream inputStream, OutputStream outputStream, Transformer transformer) throws IOException, TransformerException, ParserConfigurationException, SAXException, JAXBException {
        Objects.requireNonNull(inputStream);
        Objects.requireNonNull(outputStream);

        try (ByteArrayOutputStream repairedInput = new ByteArrayOutputStream();
             ByteArrayOutputStream transformedInput = new ByteArrayOutputStream()) {
            repair(inputStream, repairedInput);
            extend(repairedInput, transformedInput);

            TwStoriesdata stories = parse(transformedInput);
            for (TwStorydata twStorydata : stories.getTwStorydata()) {
                transformer.transform(twStorydata, outputStream);
            }
        }
    }

    private void repair(InputStream inputStream, OutputStream outputStream) throws IOException {
        try (InputStream in = new ByteArrayInputStream(IOUtils.toByteArray(inputStream))) {
            publishedRepairer.repair(in, outputStream);
        }
    }

    private void extend(ByteArrayOutputStream repairedOutput, OutputStream transformedOutput) throws IOException, TransformerException, ParserConfigurationException, SAXException {
        try(InputStream in = new ByteArrayInputStream(repairedOutput.toByteArray())) {
            extendTwineXmlTransformer.transform(in, transformedOutput);
    }
    }

    private TwStoriesdata parse(ByteArrayOutputStream transformedInput) throws IOException, JAXBException, SAXException {
        try (InputStream in = new ByteArrayInputStream(transformedInput.toByteArray())) {
            return twineArchiveParser.parse(in);
        }
    }
}
