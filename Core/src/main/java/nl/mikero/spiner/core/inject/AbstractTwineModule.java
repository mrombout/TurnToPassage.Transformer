package nl.mikero.spiner.core.inject;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import nl.mikero.spiner.core.transformer.epub.TwineStoryEpubTransformer;
import nl.mikero.spiner.core.transformer.epub.embedder.EmbedderFactory;
import nl.mikero.spiner.core.transformer.epub.embedder.HashEmbedderFactory;
import nl.mikero.spiner.core.transformer.epub.embedder.HtmlResourceEmbedder;
import nl.mikero.spiner.core.transformer.epub.embedder.ImageEmbedder;
import nl.mikero.spiner.core.transformer.epub.embedder.ResourceEmbedder;
import nl.mikero.spiner.core.twine.TwineArchiveRepairer;
import nl.mikero.spiner.core.twine.TwinePublishedRepairer;
import nl.mikero.spiner.core.twine.TwineRepairer;
import nl.mikero.spiner.core.twine.markdown.PegdownTransitionMarkdownRenderParser;
import org.apache.commons.codec.digest.DigestUtils;
import org.jsoup.parser.Parser;

/**
 * Configures Guice.
 */
public abstract class AbstractTwineModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(TwineRepairer.class).annotatedWith(ArchiveRepairer.class).to(TwineArchiveRepairer.class);
        bind(TwineRepairer.class).annotatedWith(PublishedRepairer.class).to(TwinePublishedRepairer.class);
    }

    /**
     * Provides an {@link EmbedderFactory}.
     *
     * @return an embedder factory
     */
    @Provides
    public final EmbedderFactory provideEmbedderFactoy() {
        return new HashEmbedderFactory(DigestUtils.getSha256Digest());
    }

    /**
     * Provides an {@link ImageEmbedder}.
     *
     * @return an image embedder
     */
    @Provides
    public final ImageEmbedder provideImageEmbedder() {
        return new ImageEmbedder(DigestUtils.getSha256Digest());
    }

    /**
     * Provides an {@link ResourceEmbedder}.
     *
     * @param embedderFactory embedder factory
     * @return a resource embedder
     */
    @Provides
    public final ResourceEmbedder provideHtmlResourceEmbedder(EmbedderFactory embedderFactory) {
        return new HtmlResourceEmbedder(embedderFactory, Parser.htmlParser());
    }

    /**
     * Provides a {@link TwineStoryEpubTransformer}.
     *
     * @param resourceEmbedder a resource embedder
     * @return a twine story epub transformer
     */
    @Provides
    public final TwineStoryEpubTransformer provideTwineStoryEpubTransformer(final ResourceEmbedder resourceEmbedder) {
        return new TwineStoryEpubTransformer(new PegdownTransitionMarkdownRenderParser(), resourceEmbedder);
    }
}
