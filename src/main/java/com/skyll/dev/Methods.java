package com.skyll.dev;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class Methods  {

    public static File getCardsPicOnTable(List<Card> cards, long id) {
        final double scale = 1.0/1;
        int x = (int) (758 * scale);
        int y = (int) (520 * scale);
        int step = (int) (47 * scale);
        File file = new File(getResourcesPath() + "tmp/tableCards_" + id +".png");
        boolean useShirt = false;

        try {
            BufferedImage shirt = ImageIO.read(new File(getCardsPath() + "shirt.png"));
            BufferedImage table = ImageIO.read(new File(getResourcesPath() + "table.png"));
            BufferedImage im = new BufferedImage((int)(table.getWidth()*scale), (int)(table.getHeight()*scale), BufferedImage.TYPE_INT_ARGB);
            int width = (int) (shirt.getWidth() * scale);
            int height = (int) (shirt.getHeight() * scale);

            im.getGraphics().drawImage(table, 0, 0, (int) (table.getWidth()*scale), (int) (table.getHeight()*scale), null);
            for (int i = 0; i < 5; i++) {
                if (i < cards.size() && cards.get(i) != null) {
                    im.getGraphics().drawImage(cards.get(i).getPic(), x, y, width, height, null);
                } else if (useShirt) {
                    im.getGraphics().drawImage(shirt, x, y, width, height, null);
                }
                x += width + step;
            }

            ImageIO.write(im,"png", file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
    }

    public static File getCardsPicOnUser(List<Card> cards, long id) {
        final double scale = 1.0/1;
        int step = 5;
        int frame = 50;
        File file = new File(getResourcesPath() + "tmp/userCards_" + id +".png");
        int width = (int) (cards.get(0).getPic().getWidth() * scale);
        int height = (int) (cards.get(0).getPic().getHeight() * scale);

        try {
            BufferedImage table = ImageIO.read(new File(getResourcesPath() + "table_gradient.png"));
            BufferedImage im = new BufferedImage(width*2 + step + frame, height  + frame/2, BufferedImage.TYPE_INT_ARGB);
            im.getGraphics().drawImage(table, 0, 0, (int) (table.getWidth()*scale), (int) (table.getHeight()*scale), null);

            int x = frame/2;
            for (int i = 0; i < 2; i++) {
                if (i < cards.size()) {
                    im.getGraphics().drawImage(cards.get(i).getPic(), x, frame/4, width, height, null);
                }
                x += width + step;
            }

            ImageIO.write(im,"png", file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
    }

    public static String getResourcesPath() {
        String path = Methods.class.getClassLoader().getResource("table.png").getPath();
        return path.substring(5, path.lastIndexOf("target")) + "target/classes/";
    }

    public static String getCardsPath() { return getResourcesPath() + "Big Icons/"; }

    private void getToOneSize() {
        int w = 284;
        int h = 417;
        Deck deck = new Deck();

        for (Card card : deck.getCards()) {
            try {
                File file = new File(Methods.getResourcesPath() + "newCards/" + card.toString() + ".png");
                BufferedImage newCard = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                newCard.getGraphics().drawImage(card.getPic(), 0, 0, null);

                ImageIO.write(newCard, "png", file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
