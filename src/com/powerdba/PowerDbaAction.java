package com.powerdba;

import java.util.ArrayList;

public class PowerDbaAction {
    
    public PowerDbaAction() {}
    
    public PowerDbaAction(String name) {
        this.name = name;
    }
    
    public PowerDbaAction(String name, String title, String menu1, String menu2, String menu3, boolean isRac, ArrayList queries) {
      this.name = name;
      this.menu1 = menu1;
      this.menu2 = menu2;
      this.menu3 = menu3;
      this.title = title;
      this.isRac = isRac;
      this.queries = queries;
  }

    private String name;
    private ArrayList queries;
    private String menu1;
    private String menu2;
    private String menu3;
    private String title;
    private boolean isRac=false;
    private boolean isExtended=false;
   
    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }
    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

		/**
		 * @return Returns the queries.
		 */
		public ArrayList getQueries() {
			return queries;
		}

		/**
		 * @param queries The queries to set.
		 */
		public void setQueries(ArrayList queries) {
			this.queries = queries;
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

		public boolean isExtended() {
			return isExtended;
		}

		public void setExtended(boolean isExtended) {
			this.isExtended = isExtended;
		}
		
		
    


}
