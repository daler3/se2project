    /**
     * Convert a collection of primary keys to a collection of EJBObjects.
     * (special case: single primary key).
     * Note: the order of input & output collections must be maintained.
     * Null values are preserved in both the single primary key return
     * and collection-valued return cases.
     *
     * This is called from the generated "HelloEJBHomeImpl" find* method,
     * after ejb.ejbFind**() has been called.
     * Note: postFind will not be called if ejbFindXXX throws an exception
     */
    public Object postFind(EjbInvocation inv, Object primaryKeys, 
        Object[] findParams)
        throws FinderException
    {
		if ( primaryKeys instanceof Enumeration ) {
            // create Enumeration of objrefs from Enumeration of primaryKeys
            Enumeration e = (Enumeration)primaryKeys;
            // this is a portable Serializable Enumeration
            ObjrefEnumeration objrefs = new ObjrefEnumeration();
            while ( e.hasMoreElements() ) {
				objrefs.add(refToPrimaryKey(e.nextElement(), inv.isLocal));
			}
			return objrefs;
        } else if ( primaryKeys instanceof Collection ) {
			Collection c = (Collection)primaryKeys;
            Iterator it = c.iterator();
            ArrayList objrefs = new ArrayList();  // a Serializable Collection
            while ( it.hasNext() ) {
				objrefs.add(refToPrimaryKey(it.next(), inv.isLocal));
			}
			return objrefs;
		} else {
			return refToPrimaryKey(primaryKeys, inv.isLocal);
		}
    }
	/**
	 * Get the ref to the primaryKey
	 * @param primaryKey It is the key associated to the EJBObject
	 * @param isLocal Used to check if the EJBObject is local or not.
	 * @return The EJBObject associated to the primaryKey
	 */
	private Object refToPrimaryKey(Object primaryKey, boolean isLocal){
		Object ref = null;
		if( primaryKey != null ) {
	        	if ( isLocal ) {
	                	ref = getEJBLocalObjectForPrimaryKey(primaryKey);
	                } else {
	            		ref = getEJBObjectStub(primaryKey, null);
	            	}
		}
		return ref;
	}