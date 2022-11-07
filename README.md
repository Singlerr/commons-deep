# commons-deep

------- 
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.singlerr/commons-deep/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.singlerr/commons-deep)  
commons-deep is a java library that implements struct features of C.  
In C, struct allows you to pack fields of it into one unsigned integer.  

In this sense, commons-deep allows you to create java object similarly to struct.

Gradle
------
```
    dependencies{
        annotationProcessor 'io.github.singlerr:commons-deep:<latest_version>'
        implementation 'io.github.singlerr:commons-deep:<latest_version>'
    }
```


### First, create abstract class and attach annotation specially designed
```
    @BitPacker
    public abstract class foo{
    }
```
@BitPacker notifies annotation processor that this class need to be packed.

### Second, define getter and setter
Note that getter method ### DO NOT ### has any parameters and setter method ### MUST ### has one parameter.  
That parameter value is that will be packed.

Note that The return type of getter and type of parameter of setter must be same.
```
    @BitPacker
    public abstract class foo{
        
        public abstract void setBar(int bar);
        
        public abstract int getBar();
    }
```

#### Then attach @BitSpec annotation to set bit size and variable name

The value will be packed into one integer and take up "bit size" in the integer.  
The variable name must be same along getter and setter for same value.  
The annotation processor will fetch setter and find getter with same variable name.  

```
    @BitPacker
    public abstract class foo{
        @BitSpec(bitSize = 8, variableName = "bar")
        public abstract void setBar(int bar);
         @BitSpec(bitSize = 8, variableName = "bar")
        public abstract int getBar();
    }
```
In the example above, the value "bar" will be packed into one integer with bit size 8.  
It means that it has "space" for 8 bits and if bar is unsigned int, then its range will be 0 ~ 2^8  


### Well done! Just build!
When build is successful, a class with name Packed<your abstract class name> generated.  
In the example I have explained, a class "Packedfoo" will be generated.
