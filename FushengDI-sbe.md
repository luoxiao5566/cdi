1.简介

FushengDI是基于依赖注入模式，借鉴CDI实现的一个DI轻量级框架，使用者可以支持通过构造器依赖注入，只需要将要使用接口声明并建立实现类，再使用本框架所提供的注解和规范就能由系统自动注入实现类，快捷易用

2.怎样引入FushengDI

在您的项目中引入FushengDI即可

3.@Inject注解

a.当我们使用@Inject注解标记一个字段时，CDI会自动帮助我们注入该引用接口对应的实现类

例如：我们有一个MovieFinder的接口，该接口有一个具体的实现类ColonDelimitedMovieFinder，然后我们有一个MovieLister需要依赖MovieFinder接口中的findAll()方法获取全部的影视资源，因此，我们在MovieLister中有一个MovieFinder的引用，当我们用@Inject注解标记该引用，然后调用其findAll()方法，其实调用的是其实现类ColonDelimitedMovieFinder的findAll()方法

```java
class MovieLister {
    @Inject
    private MovieFinder finder;
    
    public List<Movie> findAll() {
        return finder.findAll();
    }
}
```

```java
interface MovieFinder {
    List<Movie> findAll();
}
```

```java
class ColonDelimitedMovieFinder implements MovieFinder {
    @Override
    public List<Movie> findAll() {
        return Lists.of("Movie1", "Movie2", "Movie3");
    }
}
```



b.当我们使用@Inject注解标记一个构造函数时，CDI会自动帮助我们注入参数引用接口对应的实现类

例如：我们有一个MovieLister的实现类，该类有一个字段，类型为MovieFinder，同时也有一个构造函数，函数中的参数类型也为MovieFinder， 此时MovieFinder接口有一个具体的实现类ColonDelimitedMovieFinder，当我们用@Inject注解标记该构造函数，然后调用MovieFinder的findAll()方法，其实调用的是其实现类ColonDelimitedMovieFinder的findAll()方法

```java
class MovieLister {
    private MovieFinder finder;
    
    @Inject
    public MovieLister(MovieFinder finder) {
        this.finder = finder;
    }
    
    public List<Movie> findAll() {
        return finder.findAll();
    }
}
```

```java
interface MovieFinder {
    List<Movie> findAll();
}
```

```java
class ColonDelimitedMovieFinder implements MovieFinder {
    @Override
    public List<Movie> findAll() {
        return Lists.of("Movie1", "Movie2", "Movie3");
    }
}
```



c.当我们使用@Inject注解标记一个package-private方法时，CDI会自动帮助我们注入参数引用接口对应的实现类

例如：我们有一个MovieLister的实现类，该类有一个package-private的findAll方法，方法中有一个参数，其类型为MovieFinder， 此时MovieFinder接口有一个具体的实现类ColonDelimitedMovieFinder，当我们用@Inject注解标记该方法，然后在方法中调用MovieFinder的findAll()方法，其实调用的是其实现类ColonDelimitedMovieFinder的findAll()方法

```java
class MovieLister {
    @Inject List<Movie> findAll(MovieFinder finder) {
        return finder.findAll();
    }
}
```

```java
interface MovieFinder {
    List<Movie> findAll();
}
```

```java
class ColonDelimitedMovieFinder implements MovieFinder {
    @Override
    public List<Movie> findAll() {
        return Lists.of("Movie1", "Movie2", "Movie3");
    }
}
```

4.@Qualifier注解

当我们在通过CDI注入依赖的时候，如果接口对应多个实现类，我们需要使用@Qualifier注解定义新注解，然后在我们需要注入实现类的地方使用新注解，并且将我们需要注入的实现类也用相同的注解进行标记，这样，CDI就会注入我们标记的实现类在对应标记的地方

例如：我们有一个MovieFinder的接口，该接口有实现类ColonDelimitedMovieFinder和DatabaseMovieFinder，然后我们有一个MovieLister需要依赖MovieFinder接口中的findAll()方法获取全部的影视资源，因此，我们在MovieLister中有一个MovieFinder的引用，当我们用@Inject注解和自定义的注解标记该引用，然后用自定义的注解标记DatabaseMovieFinder实现类，最后调用MovieFinder的findAll()方法，其实调用的是其实现类DatabaseMovieFinder的findAll()方法

```java
class MovieLister {
    @Inject
    @DatabaseQualifier
    private MovieFinder finder;
    
    public List<Movie> findAll() {
        return finder.findAll();
    }
}
```

```java
interface MovieFinder {
    List<Movie> findAll();
}
```

```java
@ColonDelimitedQualifier
class ColonDelimitedMovieFinder implements MovieFinder {
    @Override
    public List<Movie> findAll() {
        return Lists.of("Movie1", "Movie2", "Movie3");
    }
}
```

```java
@DatabaseQualifier
class DatabaseMovieFinder implements MovieFinder {
    @Override
    public List<Movie> findAll() {
        return db.findAll();
    }
}
```

```java
@Documented
@Retention(RUNTIME)
@Qualifier
@interface ColonDelimitedQualifier {}
```

```java
@Documented
@Retention(RUNTIME)
@Qualifier
@interface DatabaseQualifier {}
```

5.@Named

当我们在通过CDI注入依赖的时候，如果接口对应多个实现类，需要我们在注入实现类的地方使用@Named注解，并且将我们需要注入的实现类也用@Named注解进行标记，这样，CDI就会注入我们标记的实现类在对应标记的地方

例如：我们有一个MovieFinder的接口，该接口有实现类ColonDelimitedMovieFinder和DatabaseMovieFinder，然后我们有一个MovieLister需要依赖MovieFinder接口中的findAll()方法获取全部的影视资源，因此，我们在MovieLister中有一个MovieFinder的引用，当我们用@Inject注解和@Named("database")注解标记该引用，然后用@Named("database")注解标记DatabaseMovieFinder实现类，最后调用MovieFinder的findAll()方法，其实调用的是其实现类DatabaseMovieFinder的findAll()方法

```java
class MovieLister {
    @Inject
    @Named("database")
    private MovieFinder finder;
    
    public List<Movie> findAll() {
        return finder.findAll();
    }
}
```

```java
interface MovieFinder {
    List<Movie> findAll();
}
```

```java
@Named("colonDelimited")
class ColonDelimitedMovieFinder implements MovieFinder {
    @Override
    public List<Movie> findAll() {
        return Lists.of("Movie1", "Movie2", "Movie3");
    }
}
```

```java
@Named("database")
class DatabaseMovieFinder implements MovieFinder {
    @Override
    public List<Movie> findAll() {
        return db.findAll();
    }
}
```

6.@Singleton注解

我们可以通过@Singleton注解标记我们实现类的在当前Context中的数量为一

例如：我们有一个MovieFinder的接口，该接口有一个实现类ColonDelimitedMovieFinder，然后我们有一个MovieLister1和MovieLister2需要依赖MovieFinder接口中的getClass()方法获取MovieFinder的class对象，因此，我们在MovieLister1和MovieLister2中都有一个MovieFinder的引用，当我们用@Inject注解标记该引用，然后用@Singleton注解标记ColonDelimitedMovieFinder实现类，最后调用MovieLister1中的getClass()方法和MovieLister2中的getClass()方法返回的是同一个对象

```java
class MovieLister1 {
    @Inject
    private MovieFinder finder;
    
    public Class getClass() {
        return finder.getClass();
    }
}
```

```java
class MovieLister2 {
    @Inject
    private MovieFinder finder;
    
    public Class getClass() {
        return finder.getClass();
    }
}
```

```java
interface MovieFinder {
    List<Movie> findAll();
}
```

```java
@Singleton
class ColonDelimitedMovieFinder implements MovieFinder {
    @Override
    public List<Movie> findAll() {
        return Lists.of("Movie1", "Movie2", "Movie3");
    }
}
```

