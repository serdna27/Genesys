# Genesys
Genesys is a (System|Code Generator) written in scala. This application allows to create source code from [handlerbar](http://handlebarsjs.com/) templates.
Genesys supports 3 kinds sources:
- Database
- Xml
- Json(Pending)

Genesys in order to run needs a json file that contains the basic information of the sources,the templates used for code generation and some other general configuration.Below an example of the configuration file:

```json
{  
   "source":{  
      "database":{
      	 "dbType":"sql-server|postgres|mysql|oracle",  
         "host":"your-host",
         "user":"db-user",
         "pwd":"db-pwd"
      },
      "xml":{  
         "folderContent":"folder where the xml files are located"
      },
      "json":{  
         "folderContent":"same as xml"
      }
   },
   "sourceType":"db",
   "typeMapping":{  
      "int":"int",
      "nvarchar":"string",
      "varchar":"string",
      "datetime":"DateTime",
      "bit":"bool"
   },
   "useTypeMapping":true,
   "keysFormat":[  
      {  
         "name":"name",
         "format":"pascalCase"
      },
      {  
         "name":"#fields.name",
         "format":"camelCase"
      }
   ],
   "templates":[  
      {  
         "content":"YOUR-PATH/dto.cs",
         "setting":{  
            "saveOnDisk":true,
            "directory":"YOUR-PATH/",
            "author":"Genesys beta"
         }
      }
   ]
}
 ```
 Below is an example of a c-sharp template:
 ```csharp
 namespace YourNameSpace{

	public class {{name}}:IDTO {
				        
		        {{#fields}}
		           public {{type}} {{name}} {get;set;}  
		        {{/fields}}
				         
	}
}
```
Below is the code generated based on the template:
```csharp
namespace YourNameSpace{

	public class YourClassDTO:IDTO {
				        
		           public string action {get;set;}  
		        
		           public string source {get;set;}  
		        
		           public DateTime creationDate {get;set;}  
		        
		           public int Id {get;set;}  
		              
	}
}
```


