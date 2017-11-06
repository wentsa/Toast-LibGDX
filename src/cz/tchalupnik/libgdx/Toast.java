/*
 * Copyright (c) 2017-present, Tomas Chalupnik (tchalupnik.cz)
 * All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package cz.tchalupnik.libgdx;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Align;

/**
 * Android-like toast implementation for LibGDX projects
 *
 * @author Tomas Chalupnik (tchalupnik.cz)
 */
public class Toast {
    public enum Length {
        SHORT(2),
        LONG(3.5f);

        private final float duration; // in seconds

        Length(float duration) {
            this.duration = duration;
        }
    }

    private String msg;
    private final BitmapFont font;
    private float fadingDuration;
    private final Color fontColor;
    private SpriteBatch spriteBatch = new SpriteBatch();
    private ShapeRenderer renderer = new ShapeRenderer();

    private float opacity = 1f;
    private int toastWidth;
    private int toastHeight;
    private float timeToLive;
    private float positionX, positionY; // left bottom corner
    private float fontX, fontY; // left top corner
    private int fontWidth;

    Toast(
            String text,
            Length length,
            BitmapFont font,
            Color backgroundColor,
            float fadingDuration,
            float maxRelativeWidth,
            Color fontColor,
            float positionY,
            Integer customMargin
    ) {
        this.msg = text;
        this.font = font;
        this.fadingDuration = fadingDuration;
        this.positionY = positionY;
        this.fontColor = fontColor;

        this.timeToLive = length.duration;
        renderer.setColor(backgroundColor);

        // measure text box
        GlyphLayout layoutSimple = new GlyphLayout();
        layoutSimple.setText(this.font, text);

        int lineHeight = (int) layoutSimple.height;
        fontWidth = (int) (layoutSimple.width);
        int fontHeight = (int) (layoutSimple.height);

        int margin = customMargin == null ? lineHeight * 2 : customMargin;

        float screenWidth = Gdx.graphics.getWidth();
        float maxTextWidth = screenWidth * maxRelativeWidth;
        if (fontWidth > maxTextWidth) {
            BitmapFontCache cache = new BitmapFontCache(this.font, true);
            GlyphLayout layout = cache.addText(text, 0, 0, maxTextWidth, Align.center, true);
            fontWidth = (int) layout.width;
            fontHeight = (int) layout.height;
        }

        toastHeight = fontHeight + 2 * margin;
        toastWidth = fontWidth + 2 * margin;

        positionX = (screenWidth / 2) - toastWidth / 2;

        fontX = positionX + margin;
        fontY = positionY + margin + fontHeight;
    }

    /**
     * Displays toast<br/>
     * Must be called at the end of {@link Game#render()}<br/>
     * @param delta {@link Graphics#getDeltaTime()}
     * @return activeness of the toast (true while being displayed, false otherwise)
     */
    public boolean render(float delta) {
        timeToLive -= delta;

        if (timeToLive < 0) {
            return false;
        }

        renderer.begin(ShapeRenderer.ShapeType.Filled);
        renderer.circle(positionX, positionY + toastHeight / 2, toastHeight / 2);
        renderer.rect(positionX, positionY, toastWidth, toastHeight);
        renderer.circle(positionX + toastWidth, positionY + toastHeight / 2, toastHeight / 2);
        renderer.end();

        spriteBatch.begin();

        if (timeToLive > 0 && opacity > 0.15) {
            if (timeToLive < fadingDuration) {
                opacity = timeToLive / fadingDuration;
            }

            font.setColor(fontColor.r, fontColor.g, fontColor.b, fontColor.a * opacity);
            font.draw(spriteBatch, msg, fontX, fontY, fontWidth, Align.center, true);
        }

        spriteBatch.end();

        return true;
    }

    /**
     * Factory for creating toasts
     */
    public static class ToastFactory {

        private BitmapFont font;
        private Color backgroundColor = new Color(55f / 256, 55f / 256, 55f / 256, 1);
        private Color fontColor = new Color(1, 1, 1, 1);
        private float positionY;
        private float fadingDuration = 0.5f;
        private float maxRelativeWidth = 0.65f;
        private Integer customMargin;

        private ToastFactory() {
            float screenHeight = Gdx.graphics.getHeight();
            float bottomGap = 100;
            positionY = bottomGap + ((screenHeight - bottomGap) / 10);
        }

        /**
         * Creates new toast
         * @param text message
         * @param length toast duration
         * @return newly created toast
         */
        public Toast create(String text, Length length) {
            return new Toast(
                    text,
                    length,
                    font,
                    backgroundColor,
                    fadingDuration,
                    maxRelativeWidth,
                    fontColor,
                    positionY,
                    customMargin);
        }

        /**
         * Builder for creating factory
         */
        public static class Builder {

            private boolean built = false;
            private ToastFactory factory = new ToastFactory();

            /**
             * Specify font for toasts
             * @param font font
             * @return this
             */
            public Builder font(BitmapFont font) {
                check();
                factory.font = font;
                return this;
            }

            /**
             * Specify background color for toasts.<br/>
             * Note: Alpha channel is not supported (yet).<br/>
             * Default: rgb(55,55,55)
             * @param color background color
             * @return this
             */
            public Builder backgroundColor(Color color) {
                check();
                factory.backgroundColor = color;
                return this;
            }

            /**
             * Specify font color for toasts.<br/>
             * Default: white
             * @param color font color
             * @return this
             */
            public Builder fontColor(Color color) {
                check();
                factory.fontColor = color;
                return this;
            }

            /**
             * Specify vertical position for toasts<br/>
             * Default: bottom part
             * @param positionY vertical position of bottom left corner
             * @return this
             */
            public Builder positionY(float positionY) {
                check();
                factory.positionY = positionY;
                return this;
            }

            /**
             * Specify fading duration for toasts<br/>
             * Default: 0.5s
             * @param fadingDuration duration in seconds which it takes to disappear
             * @return this
             */
            public Builder fadingDuration(float fadingDuration) {
                check();
                if (fadingDuration < 0) {
                    throw new IllegalArgumentException("Duration must be non-negative number");
                }
                factory.fadingDuration = fadingDuration;
                return this;
            }

            /**
             * Specify max text width for toasts<br/>
             * Default: 0.65
             * @param maxTextRelativeWidth max text width relative to screen (Eg. 0.5 = max text width is equal to 50% of screen width)
             * @return this
             */
            public Builder maxTextRelativeWidth(float maxTextRelativeWidth) {
                check();
                factory.maxRelativeWidth = maxTextRelativeWidth;
                return this;
            }

            /**
             * Specify text margin for toasts<br/>
             * Default: line height
             * @param margin margin in px
             * @return this
             */
            public Builder margin(int margin) {
                check();
                factory.customMargin = margin;
                return this;
            }

            /**
             * Builds factory
             * @return new factory
             */
            public ToastFactory build() {
                check();
                if (factory.font == null) {
                    throw new IllegalStateException("Font is not set");
                }
                built = true;
                return factory;
            }

            private void check() {
                if (built) {
                    throw new IllegalStateException("Builder can be used only once");
                }
            }
        }
    }

}
