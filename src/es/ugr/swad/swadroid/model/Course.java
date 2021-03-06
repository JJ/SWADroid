/*
 *  This file is part of SWADroid.
 *
 *  Copyright (C) 2010 Juan Miguel Boyero Corral <juanmi1982@gmail.com>
 *
 *  SWADroid is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  SWADroid is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with SWADroid.  If not, see <http://www.gnu.org/licenses/>.
 */
package es.ugr.swad.swadroid.model;

import java.util.Hashtable;

import org.ksoap2.serialization.PropertyInfo;

/**
 * Class for store a course
 * @author Juan Miguel Boyero Corral <juanmi1982@gmail.com>
 */
public class Course extends Model {
	/**
	 * Course name
	 */
	private String name;
	private int userRole;
	private static PropertyInfo PI_id = new PropertyInfo();
	private static PropertyInfo PI_name = new PropertyInfo();
	private static PropertyInfo PI_userRole = new PropertyInfo();
	@SuppressWarnings("unused")
	private static PropertyInfo[] PI_PROP_ARRAY =
{
		PI_id,
		PI_name,
		PI_userRole
};

	/**
	 * Constructor
	 * @param id Course identifier
	 * @param name Course name
	 */
	public Course(long id, String name, int userRole) {
		super(id);
		this.name = name;
		this.userRole = userRole;
	}

	/**
	 * Gets course name
	 * @return Course name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets course name
	 * @param name Course name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets user role inside the course
	 * @return user role (2 = student, 3 = teacher)
	 */
	public int getUserRole(){
		return userRole;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + userRole;
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Course [name=" + name + ", getId()=" + getId() + " getUserRole()="+ getUserRole()+"]";
	}

	/* (non-Javadoc)
	 * @see org.ksoap2.serialization.KvmSerializable#getProperty(int)
	 */
	public Object getProperty(int param) {
		Object object = null;
		switch(param)
		{
		case 0 : object = this.getId();break;
		case 1 : object = name;break;
		case 2 : object = userRole;break;
		}

		return object;
	}

	/* (non-Javadoc)
	 * @see org.ksoap2.serialization.KvmSerializable#getPropertyCount()
	 */
	public int getPropertyCount() {
		return 3;
	}

	/* (non-Javadoc)
	 * @see org.ksoap2.serialization.KvmSerializable#getPropertyInfo(int, java.util.Hashtable, org.ksoap2.serialization.PropertyInfo)
	 */
	public void getPropertyInfo(int param, @SuppressWarnings("rawtypes") Hashtable arg1, PropertyInfo propertyInfo) {
		switch(param){
		case 0:
			propertyInfo.type = PropertyInfo.LONG_CLASS;
			propertyInfo.name = "id";
			break;   
		case 1:
			propertyInfo.type = PropertyInfo.STRING_CLASS;
			propertyInfo.name = "name";
			break; 
		case 2:
			propertyInfo.type = PropertyInfo.INTEGER_CLASS;
			propertyInfo.name = "userRole";
			break;
		}
	}

	/* (non-Javadoc)
	 * @see org.ksoap2.serialization.KvmSerializable#setProperty(int, java.lang.Object)
	 */
	public void setProperty(int param, Object obj) {
		switch(param)
		{
		case 0  : this.setId((Long)obj); break;
		case 1  : name = (String)obj; break;
		case 2  : userRole = (Integer)obj; break;
		}    
	}

	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj)) return false;
		Course other = (Course) obj;
		if(name.compareTo(other.getName()) != 0) return false;
		if(userRole != other.getUserRole())	return false;
		return true; 
	}

}