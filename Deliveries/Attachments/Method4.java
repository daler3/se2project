    /**
    * Called only from the Persistence Manager for EJB2.0 CMP EntityBeans.
    * Called only during cascade delete.
    * This is a private API between the PM and Container because there
    * is no standard API defined in EJB2.0 for the PM to get an EJBLocalObject
    * for a primary key (findByPrimaryKey cant be used because it may
    * not run in the same tx).
    * 
    * Example 1:
    *    A cascadeDeletes B and B calls getA() (expected return value: null)
    *
    *    In the above case, getA() eventualy calls getEJBLocalObjectForPrimaryKey(PK_of_A, Ctx_of_B)
    *    We first check if B is in the process of being cascade deleted by checking the 
    *    cascadeDeleteBeforeEJBRemove flag. If this flag is true, only then we bother to check if
    *    the Context associated with the PK_of_A in this transaction is marked for cascade delete
    *    which can be figured out by checking isCascadeDeleteAfterSuperEJBRemove() in A's context.
    *    If A is marked for cascade delete then we return null else the EJBLocalObject associated
    *    with A.
    *  
    * Example 2:
    *    C cascadeDeletes B and B calls getA() (expected return value: EJBLocalObject for PK_of_A)
    *
    *    In the above case, getA() eventualy calls getEJBLocalObjectForPrimaryKey(PK_of_A, Ctx_of_B)
    *    We first check if B is in the process of being cascade deleted by checking the 
    *    cascadeDeleteBeforeEJBRemove flag. This flag will be true, and hence we check if
    *    the Context associated with the PK_of_A in this transaction is marked for cascade delete
    *    which can be figured out by checking isCascadeDeleteAfterSuperEJBRemove() in A's context.
    *    In this case this flag will be false and hence we return the ejbLocalObject

    * Example 3:
    *    B is *NOT* cascade deleted and B calls getA() (expected return value: EJBLocalObject for PK_of_A)
    *
    *    In the above case, getA() eventualy calls getEJBLocalObjectForPrimaryKey(PK_of_A, Ctx_of_B)
    *    We first check if B is in the process of being cascade deleted by checking the 
    *    cascadeDeleteBeforeEJBRemove flag. This flag will be FALSE, and hence we do not make
    *    any further check and return the EJBLocalObject associated with A
    *
    * @param pkey The primary key for which the EJBLocalObject is required
    * @param ctx The context associated with the bean from which the accessor method is invoked
    * @return The EJBLocalObject associated with the PK or null if it is cascade deleted.
    *
    */
    public EJBLocalObject getEJBLocalObjectForPrimaryKey
        (Object pkey, EJBContext ctx) {
        // EntityContextImpl should always be used in conjunction with EntityContainer so we can always cast
        assert ctx instanceof EntityContextImpl;
        EntityContextImpl context = (EntityContextImpl) ctx;
        EJBLocalObjectImpl ejbLocalObjectImpl = 
            internalGetEJBLocalObjectImpl(pkey);

        if (context.isCascadeDeleteBeforeEJBRemove()) {
            JavaEETransaction current = null;
            try {
                current = (JavaEETransaction) transactionManager.getTransaction();
            } catch ( SystemException ex ) {
                throw new EJBException(ex);
            }

	        ActiveTxCache activeTxCache = (current == null) ? null :
		        (ActiveTxCache) (ejbContainerUtilImpl.getActiveTxCache(current));
            if (activeTxCache != null) {
		        EntityContextImpl ctx2 = (EntityContextImpl)
			        activeTxCache.get(this, pkey);
		        if ((ctx2 != null) && 
		                (ctx2.isCascadeDeleteAfterSuperEJBRemove())) {
		            return null;
		        }
	        }

        }

        return (EJBLocalObject) ejbLocalObjectImpl.getClientObject();
    }