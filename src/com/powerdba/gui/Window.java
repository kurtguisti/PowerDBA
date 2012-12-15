/*
 * Created by IntelliJ IDEA.
 * User: kguisti
 * Date: Nov 2, 2002
 * Time: 7:52:19 PM
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package com.powerdba.gui;

import java.util.ArrayList;

public class Window {
	
    public Window() {}
    
    private String title = new String();
    private String menu1;
    private String menu2;
    private String menu3;
    private ArrayList pages = new ArrayList();
    private boolean isMultiple = true;
    private boolean isRac = false;
    
    public Window(String title, String menu1, String menu2, ArrayList pages) {
        this.title = title;
        this.menu1 = menu1;
        this.menu2 = menu2;
        this.pages = pages;
    }
    
    public Window(String title, String menu1, String menu2, ArrayList pages, boolean isRac) {
      this.title = title;
      this.menu1 = menu1;
      this.menu2 = menu2;
      this.pages = pages;
      this.isRac = isRac;
    }


    /**
     * @return Returns the isMultiple.
     */
    public boolean isMultiple() {
        return isMultiple;
    }
    /**
     * @param isMultiple The isMultiple to set.
     */
    public void setMultiple(boolean isMultiple) {
        this.isMultiple = isMultiple;
    }
    /**
     * @return Returns the isRac.
     */
    public boolean isRac() {
        return isRac;
    }
    /**
     * @param isRac The isRac to set.
     */
    public void setRac(boolean isRac) {
        this.isRac = isRac;
    }

    /**
     * @return Returns the page.
     */
    public ArrayList getPages() {
        return pages;
    }
    /**
     * @param page The page to set.
     */
    public void setPages(ArrayList pages) {
        this.pages = pages;
    }
    /**
     * @return Returns the title.
     */
    public String getTitle() {
        return title;
    }
    /**
     * @param title The title to set.
     */
    public void setTitle(String title) {
        this.title = title;
    }

		/**
		 * @return Returns the menu1.
		 */
		public String getMenu1() {
			return menu1;
		}

		/**
		 * @param menu1 The menu1 to set.
		 */
		public void setMenu1(String menu1) {
			this.menu1 = menu1;
		}

		/**
		 * @return Returns the menu2.
		 */
		public String getMenu2() {
			return menu2;
		}

		/**
		 * @param menu2 The menu2 to set.
		 */
		public void setMenu2(String menu2) {
			this.menu2 = menu2;
		}

		/**
		 * @return Returns the menu3.
		 */
		public String getMenu3() {
			return menu3;
		}

		/**
		 * @param menu3 The menu3 to set.
		 */
		public void setMenu3(String menu3) {
			this.menu3 = menu3;
		}
    
    
}
    