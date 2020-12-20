package me.zeroeightsix.kami.gui.rgui.render.util;

import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;

public class Framebuffer {

    private int WIDTH = Display.getWidth();
    private int HEIGHT = Display.getHeight();

    private int framebufferID;
    private int framebufferTexture;
    private int framebufferDepthbuffer;

    public Framebuffer() {// call when loading the game
        this(Display.getWidth(), Display.getHeight());
    }

    public Framebuffer(int WIDTH, int HEIGHT) {
        this.WIDTH = WIDTH;
        this.HEIGHT = HEIGHT;
        initialiseFramebuffer();
    }

    public void cleanUp() {// call when closing the game
        GL30.glDeleteFramebuffers(framebufferID);
        GL11.glDeleteTextures(framebufferTexture);
        GL30.glDeleteRenderbuffers(framebufferDepthbuffer);
    }

    public void bindFrameBuffer() {// call before rendering to this FBO
        bindFrameBuffer(framebufferID, WIDTH, HEIGHT);
    }

    public void unbindFramebuffer() {// call to switch to default frame buffer
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        GL11.glViewport(0, 0, Display.getWidth(), Display.getHeight());
    }

    public int getFramebufferTexture() {// get the resulting texture
        return framebufferTexture;
    }

    private void initialiseFramebuffer() {
        framebufferID = createFrameBuffer();
        framebufferTexture = createTextureAttachment(WIDTH, HEIGHT);
        framebufferDepthbuffer = createDepthBufferAttachment(WIDTH, HEIGHT);
        unbindFramebuffer();
    }

    private void bindFrameBuffer(int frameBuffer, int width, int height) {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);// To make sure the texture isn't bound
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, frameBuffer);
        GL11.glViewport(0, 0, width, height);
    }

    private int createFrameBuffer() {
        int frameBuffer = GL30.glGenFramebuffers();
        // generate name for frame buffer
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, frameBuffer);
        // create the framebuffer
        GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
        // indicate that we will always render to colour attachment 0
        return frameBuffer;
    }

    private int createTextureAttachment(int width, int height) {
        int texture = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, width, height,
            0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0,
            texture, 0);
        return texture;
    }

    private int createDepthTextureAttachment(int width, int height) {
        int texture = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL14.GL_DEPTH_COMPONENT32, width, height,
            0, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, (ByteBuffer) null);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT,
            texture, 0);
        return texture;
    }

    private int createDepthBufferAttachment(int width, int height) {
        int depthBuffer = GL30.glGenRenderbuffers();
        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, depthBuffer);
        GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL11.GL_DEPTH_COMPONENT, width,
            height);
        GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT,
            GL30.GL_RENDERBUFFER, depthBuffer);
        return depthBuffer;
    }

    public void framebufferClear() {
        bindFrameBuffer();

        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

        this.unbindFramebuffer();
    }

    public int getWidth() {
        return WIDTH;
    }

    public int getHeight() {
        return HEIGHT;
    }
}