# Genesys
Genesys is a (Code Generator) written in scala. This application allows you to create source code from database metadata using [handlerbar](http://handlebarsjs.com/) templates,if you want to try it go to releases and Download the latest file and place it in your path. Check sample Below:

![](http://i.imgur.com/FuVHa1w.gifv)

Genesys supports 3 kinds of sources:
- Database
- Xml(Limited)
- Json(Pending)

Before running the generator, we first need a json file that contains the basic information of the sources,the templates used for code generation and some others configuration.Below an example of the configuration file:

```json
{  
   "source":{  
      "database":{  
         "host":"jdbc:database://host/db",
         "user":"user",
         "pwd":"password",
         "dbType":"sql-server|postgres|mysql|oracle",
      },
      "xml":{  
         "folderContent":"folder where the xml files are located."
      },
      "json":{  
         "folderContent":"same as xml."
      }
   },
   "sourceType":"db",
   "typeMapping":{  
      "int":"int",
      "smallint":"int"
      "nvarchar":"string",
      "varchar":"string",
      "datetime":"DateTime",
      "bit":"bool",
      "text":"string",
      "serial":"int"
   },
   "useTypeMapping":true,
   "keysFormat":[  
      {  
         "name":"name",
         "format":"pascalCase"
      },
      {  
         "name":"#fields.name",
         "format":"pascalCase"
      }
   ],
   "templates":[  
     {  
         "content":"YOUR-PATH/dto.cs",
         "setting":{  
            "saveOnDisk":true,
            "suffixName":"DTO",
            "directory":"YOUR-PATH/"
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


