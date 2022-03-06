package subRipper;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ImagePanel extends JLabel {
   private static final long serialVersionUID = 1L;
   private BufferedImage image;

/*   public ImagePanel(BufferedImage image) {
      this.image = image;
   }*/
   
   public ImagePanel() {	      
   }

   public ImagePanel(String text) {
	   this.setText(text);
   }   
   
   public void loadBuffer(BufferedImage image, int imgW, int imgH) {
	   prndeb(5,"enter loadBuffer");
	   
	   prndeb(9,"image dimensions=" + image.getWidth() + 'x' + image.getHeight());
	   	  
	   BufferedImage tmp = new BufferedImage(imgW, image.getHeight()*imgW/image.getWidth(), BufferedImage.TYPE_INT_RGB);
	   tmp = scale(image, imgW, image.getHeight()*imgW/image.getWidth());

	   prndeb(9,"image dimensions=" + tmp.getWidth() + 'x' + tmp.getHeight());
	   	  
	   //this.image = scale(image, 800, image.getHeight()/800*600);
	   this.setBounds(0, (imgH-tmp.getHeight())/2, imgW,image.getHeight()*imgW/image.getWidth());
	   this.setIcon(new ImageIcon(tmp));
	   this.repaint();
	   
	   prndeb(5,"exit loadBuffer");	   
   }
   
   public BufferedImage getImage() {
	   ImageIcon icon = (ImageIcon)this.getIcon();
	   BufferedImage img = null;
	   
 	   if(icon==null) {
 		   prndeb(1,"ERROR: getImage: No image available");
 		   
 	   }
 	   else {	    	   
 		   img = (BufferedImage)((Image) icon.getImage());
 	   }
	   return img;
   }
   
   public int getPixelColor(int x, int y) {
	  // System.out.println("coordinates=" + x + "," + y);
      // System.out.println("image dimensions=" + this.getImage().getWidth() + 'x' + this.getImage().getHeight());
	   BufferedImage img = this.getImage();
	   int pixel=-9999;
	   
	   if(img!=null)  pixel = img.getRGB(x, y);	   

	   //printPixelARGB(pixel);	   
	   return pixel;
   }
   
   /*
   public void paint(Graphics g) {
      g.drawImage(image, 0, 0, this);
   }

   public void loadOriginalImage(File file) {
      try {
         image = ImageIO.read(file);
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   public void createArtWork() {
      if (image == null)
         return;
      try {
         Graphics g = image.getGraphics();
         g.setColor(Color.red);
         g.drawString("Picture speaks thousand words", 50, 50);
         g.drawImage(ImageIO.read(new File("/home/mano/Pictures/cartoons/blob.jpg")), 120, 100, null);
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   public void convertToGrayscale() {
      if (image == null)
         return;
      for (int i = 0; i < image.getHeight(); i++) {
         for (int j = 0; j < image.getWidth(); j++) {
            Color imageColor = new Color(image.getRGB(j, i));
            int rgb = (int) (imageColor.getRed() * 0.299)
               + (int) (imageColor.getGreen() * 0.587)
               + (int) (imageColor.getBlue() * 0.114);
            Color newColor = new Color(rgb, rgb, rgb);
            image.setRGB(j, i, newColor.getRGB());
         }
      }
   }
   */
   
   private static BufferedImage scale(BufferedImage src, int w, int h)
   {
       BufferedImage img = 
               new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
       int x, y;
       int ww = src.getWidth();
       int hh = src.getHeight();
       int[] ys = new int[h];
       for (y = 0; y < h; y++)
           ys[y] = y * hh / h;
       for (x = 0; x < w; x++) {
           int newX = x * ww / w;
           for (y = 0; y < h; y++) {
               int col = src.getRGB(newX, ys[y]);
               img.setRGB(x, y, col);
           }
       }
       return img;
   }
   
   public void printPixelARGB(int pixel) {
	    int alpha = (pixel >> 24) & 0xff;
	    int red = (pixel >> 16) & 0xff;
	    int green = (pixel >> 8) & 0xff;
	    int blue = (pixel) & 0xff;
	    prndeb(9,"argb: " + alpha + ", " + red + ", " + green + ", " + blue);
	  }
   
	public static void prndeb(int lvl, String txt) {
		int debugLevel = SubRipper.debugLevel;
  		if (lvl<= debugLevel)
  			System.out.println(txt);
  	}
  	
}