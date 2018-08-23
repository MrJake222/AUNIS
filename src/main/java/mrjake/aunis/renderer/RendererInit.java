package mrjake.aunis.renderer;

import static org.lwjgl.opengl.GL11.GL_QUAD_STRIP;
import static org.lwjgl.opengl.GL11.GL_TRIANGLE_FAN;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glTexCoord2f;
import static org.lwjgl.opengl.GL11.glVertex3f;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class RendererInit {
	final float eventHorizonRadius = 3.790975f;
	
	private final int quads = 16;
	private final int sections = 36 * 2;
	private final float sectionAngle = (float) (2*Math.PI/sections);
	
	private final float innerCircleRadius = 0.25f;
	private final float quadStep = (eventHorizonRadius - innerCircleRadius) / quads;
	
	private List<Float> offsetList = new ArrayList<Float>();
	// private long horizonStateChange = 0;
	private List<Float> sin = new ArrayList<Float>();
	private List<Float> cos = new ArrayList<Float>();
	
	private List<Float> quadRadius = new ArrayList<Float>();
	
	InnerCircle innerCircle;
	List<QuadStrip> quadStrips = new ArrayList<QuadStrip>();
	
	public RendererInit() {
		// Load chevron textures
		for (int i=0; i<=10; i++) {
			ResourceLocation resource = new ResourceLocation( "aunis:textures/tesr/stargate/chevron/chevron"+i+".png" );
					
			ITextureObject itextureobject = new SimpleTexture(resource);
			Minecraft.getMinecraft().getTextureManager().loadTexture(resource, itextureobject);
		}
		
		initEventHorizon();
		initKawoosh();
	}
	
	Random rand = new Random();
	
	private float getRandomFloat() {
		return rand.nextFloat()*2-1;
	}
	
	private float getOffset(int index, float tick) {
		return MathHelper.sin( tick/4f + offsetList.get(index) ) / 24f;
	}
	
	private float toUV(float coord) {
		return (coord + 1) / 2f;
	}
	
	private void initEventHorizon() {
		for (int i=0; i<sections*(quads+1); i++) {
			offsetList.add( getRandomFloat() * 3 );
		}
		
		for (int i=0; i<=sections; i++) {
			sin.add( MathHelper.sin(sectionAngle * i) );
			cos.add( MathHelper.cos(sectionAngle * i) );
		}
		
		innerCircle = new InnerCircle();
		
		for (int i=0; i<=quads; i++) {
			quadRadius.add( innerCircleRadius + quadStep*i );
		}
		
		for (int i=0; i<quads; i++) {
			quadStrips.add( new QuadStrip(i) );
		}

		// horizonStateChange = world.getTotalWorldTime();
	}
	
	class InnerCircle {
		private List<Float> x = new ArrayList<Float>();
		private List<Float> y = new ArrayList<Float>();
		
		private List<Float> tx = new ArrayList<Float>();
		private List<Float> ty = new ArrayList<Float>();
		
		public InnerCircle() {
			float texMul = (innerCircleRadius / eventHorizonRadius);
			
			for (int i=0; i<sections; i++) {			
				x.add( sin.get(i) * innerCircleRadius );
				y.add( cos.get(i) * innerCircleRadius );
				
				tx.add( toUV( sin.get(i) * texMul ) );
				ty.add( toUV( cos.get(i) * texMul ) );
			}
		}
		
		public void render(float tick) {
			render(tick, false, null);
		}
		
		public void render(float tick, boolean white, Float alpha) {
			if (white) {
				GlStateManager.disableTexture2D();
				if (alpha > 0.5f)
					alpha = 1.0f - alpha;
			}
			
			glBegin(GL_TRIANGLE_FAN);
			
			if (alpha != null) glColor4f(1.0f, 1.0f, 1.0f, alpha.floatValue());
			if (!white) glTexCoord2f(0.5f, 0.5f);
			
			glVertex3f(0, 0, 0);
			
			int index = 0;
			for (int i=sections; i>=0; i--) {
				if (i == sections)
					index = 0;
				else
					index = i;
				
				if (!white) glTexCoord2f( tx.get(index), ty.get(index) );
				glVertex3f( x.get(index), y.get(index), getOffset(index, tick) );
			}

			glEnd();
			
			if (alpha != null) glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
			if (white) GlStateManager.enableTexture2D();
		}
	}
	
	class QuadStrip {
		private List<Float> x = new ArrayList<Float>();
		private List<Float> y = new ArrayList<Float>();
		
		private List<Float> tx = new ArrayList<Float>();
		private List<Float> ty = new ArrayList<Float>();
		
		private int quadStripIndex;
		
		public QuadStrip(int quadStripIndex) {
			// this(quadStripIndex, quadRadius.get(quadStripIndex), quadRadius.get(quadStripIndex+1), false, 0);
			this( quadStripIndex, quadRadius.get(quadStripIndex), quadRadius.get(quadStripIndex+1), null );
		}
		
		public QuadStrip(int quadStripIndex, float innerRadius, float outerRadius/*, boolean randomizeRadius*/, Float tick) {
			this.quadStripIndex = quadStripIndex; 
			recalculate(innerRadius, outerRadius, tick);
		}
		
		public void recalculate(float innerRadius, float outerRadius, Float tick) {
			//this.quadStripIndex = quadStripIndex; 
			
			List<Float> radius = new ArrayList<Float>();
			List<Float> texMul = new ArrayList<Float>();
			
			/*radius.add( quadRadius.get( quadStripIndex   ) ); // Inner
			  radius.add( quadRadius.get( quadStripIndex+1 ) ); // Outer */
			
			radius.add( innerRadius );
			radius.add( outerRadius );
			
			for (int i=0; i<2; i++)
				texMul.add( radius.get(i) / eventHorizonRadius );
			
			for (int k=0; k<2; k++) {
				for (int i=0; i<sections; i++) {
					float rad = radius.get(k);
					
					if (tick != null) {
						rad += getOffset(i, tick) * 2;
					}
					
					x.add( rad * sin.get(i) );
					y.add( rad * cos.get(i) );
					
					tx.add( toUV( sin.get(i) * texMul.get(k) ) );
					ty.add( toUV( cos.get(i) * texMul.get(k) ) );
				}
			}
		}
		
		public void render(float tick) {
			render(tick, false, null);
		}
		
		public void render(float tick, boolean white, Float alpha) {
			render(tick, null, null, white, alpha);
		}
		
		public void render(float tick, Float outerZ, Float innerZ) {
			render(tick, outerZ, innerZ, false, null);
		}
		
		public void render(float tick, Float outerZ, Float innerZ, boolean white, Float alpha) {
			if (white) {
				GlStateManager.disableTexture2D();
				if (alpha > 0.5f)
					alpha = 1.0f - alpha;
			}
			
			if (alpha != null) glColor4f(1.0f, 1.0f, 1.0f, alpha.floatValue());
			
			glBegin(GL_QUAD_STRIP);
			
			int index = 0;
			
			for (int i=sections; i>=0; i--) {
				if (i == sections)
					index = 0;
				else
					index = i;
				
				float z;
				
				if (outerZ != null) z = outerZ.floatValue();
				else z = getOffset(index + sections*quadStripIndex, tick);
				
				if (!white) glTexCoord2f( tx.get(index), ty.get(index) );
				glVertex3f( x.get(index), y.get(index),  z );
				
				index = index + sections;
				
				if (innerZ != null) z = innerZ.floatValue();
				else z = getOffset(index + sections*quadStripIndex, tick);
				
				if (!white) glTexCoord2f( tx.get(index), ty.get(index) );
				glVertex3f( x.get(index), y.get(index), z );
			}
			
			glEnd();
			
			if (white) GlStateManager.enableTexture2D();
			if (alpha != null) glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		}
	}
	
	// --------------------------------------------------------------------------------------------------------------------
	
	final float kawooshRadius = 2.5f;
	private final float kawooshSize = 7f;
	private final float kawooshSections = 128; 
	
	Map<Float, Float> Z_RadiusMap = new LinkedHashMap<Float, Float>();
	
	// Generate kawoosh shape using 4 functions
	private void initKawoosh() {
		float begin = 0;
		float end   = 0.545f;
		
		float rng = end - begin;

		float step = rng / kawooshSections;
		boolean first = true;
		
		float scaleX = kawooshSize / rng;
		float scaleY = 1;
		
		for (int i=0; i<=kawooshSections; i++) {
			float x = begin + step*i;
			float y = 0;
			
			float border1 = 0.2575f;
			float border2 = 0.4241f;
			float border3 = 0.4577f;
			
			
			if ( x >= 0 && x <= border1 ) {
				float a = 2f;
				float b = -4.7f;
				float c = 2.1f;
				
				float p = x + (b/20f);
				y = (a/2f) * (p*p) + (c/30f);
				
				if (first) {
				first = false;
				scaleY = kawooshRadius / y;
				// Aunis.info("radius: " + kawooshRadius + "  y: " + y + "  scale: "+kawooshScaleFactor);
				}
			}
			
			else if ( x > border1 && x <= border2 ) {
				float a = 1.4f;
				float b = -4.3f;
				float c = 1.4f;
				
				float p = x + (b/20f);
				y = (a/5f) * (p*p) + (c/20f);
			}
			
			else if ( x > border2 && x <= border3 ) {
				float a = -7.4f;
				float b = -8.6f;
				float c = 3.3f;
				
				float p = x + (b/20f);
				y = a * (p*p) + (c/40f);
			}
			
			else if ( x > border3 && x <= 0.545f ) {
				float a = 5.2f;
				
				y = (float) (a/20f * Math.sqrt( 0.545f - x ));
			}
			
			Z_RadiusMap.put(x*scaleX, y*scaleY);
		}
	}
	
}
