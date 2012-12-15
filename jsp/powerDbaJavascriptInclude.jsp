    <script>                                                           
       var win;                                                        
       function explain(myForm, hash, dbname, sid) {                      
           myForm.target.value     = 1115;    
           myForm.formaction.value = 1115;    
           myForm.source.value     = "0";                    
           myForm.mode.value       = "0";                    
           myForm.database.value   = dbname;                           
           myForm.sid.value        = sid;                              
           myForm.hash.value       = hash;                             
           myForm.submit();                                            
       } 
       function managetrx(myForm, trx, dbname, trxaction) {                       
           myForm.formaction.value = <%= PowerDbaActions.DB_STREAMS_TRX %>;                      
           myForm.database.value   = dbname;                                                     
           myForm.streamstrx.value = trx;
           myForm.subaction.value  = trxaction;                             
           myForm.submit();                                            
       } 
       function managesess(myForm, sid, dbname, sessaction) {                       
           myForm.formaction.value = <%= PowerDbaActions.DB_SESS_ZOOM %>;                      
           myForm.database.value   = dbname;                                                     
           myForm.sid.value = sid;
           myForm.subaction.value  = sessaction;                             
           myForm.submit();                                            
       } 
       function managecapt(myForm, capt, dbname, subaction) {                       
           myForm.formaction.value = <%= PowerDbaActions.DB_CAPTURE_DETAIL %>;                      
           myForm.database.value   = dbname;                                                     
           myForm.key.value        = capt;
           myForm.subaction.value  = subaction;                             
           myForm.submit();                                            
       }   
       function managejob(myForm, jobid, dbname, subaction) {                       
           myForm.formaction.value = <%= PowerDbaActions.DB_JOBS_DETAIL %>;                      
           myForm.database.value   = dbname;                                                     
           myForm.key.value        = jobid;
           myForm.subaction.value  = subaction;                             
           myForm.submit();                                            
       }                                                                       
       function explainsql(myForm, dbname) {                      
           myForm.target.value     = 1115;    
           myForm.formaction.value = 1115;    
           myForm.source.value     = "0";                    
           myForm.mode.value       = "0";                    
           myForm.database.value   = dbname;                           
           myForm.submit();                                            
       }                                                               
       function editRule(key) {                                          
         if ( win ) {                                                  
            win.close();                                               
         }                                                             
         win = window.open('editor.jsp?action=10001&key='+key+'&database=home-pc','editPopup','width=700,height=450,scrollbars=yes');
         win.focus();                                                  
       } 
       function sqlpop(database,query) {                                          
         if ( win ) {                                                  
            win.close();                                               
         }                                                             
         win = window.open('sqlPopup.jsp?database='+database+'&query='+query,'SqlPopup','width=700,height=450,scrollbars=yes');
         win.focus();                                                  
       }                                                                
       function go(myForm, jsp, dbaction, dbname) {                        
           myForm.formaction.value = dbaction;                                             
           myForm.database.value   = dbname;                                                       
           myForm.submit();                                            
       }                                                               
       function refresh(myForm, jsp, dbaction) {                    
           myForm.target.value     = dbaction;                         
           myForm.formaction.value = dbaction;                          
           myForm.source.value     = "0";                    
           myForm.mode.value       = "0";                    
           myForm.submit();                                            
       }                                                               
    </script> 
