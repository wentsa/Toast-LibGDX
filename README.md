# Toast for LibGDX
Android-like **Toast** implementation for [LibGDX](https://libgdx.badlogicgames.com/) projects. (

Tested with version 1.9.6

How to
-----------
1. Include [`Toast`](src/cz/tchalupnik/libgdx/Toast.java) in your LibGDX project
2. Create `ToastFactory` for your toasts (using `Builder`):
```java
Toast.ToastFactory toastFactory = new Toast.ToastFactory.Builder()
                .font(font)
                .build();
```
3. Create toast:
```java
Toast toast = toastFactory.create("Hello world!", Toast.Length.LONG);
```
4. Display it in `render()`:
```java
toast.render(Gdx.graphics.getDeltaTime());
```


<br/>
<br/>

You can set some optional attributes while creating the factory:
```java
Toast.ToastFactory toastFactory = new Toast.ToastFactory.Builder()
                .font(font)
                .backgroundColor(new Color(0.5f, 0.5f, 0.5f, 1f))
                .fadingDuration(1.2f)
                .fontColor(new Color(1f, 0.2f, 0.68f, 0.75f))
                .margin(20)
                .maxTextRelativeWidth(0.5f)
                .positionY(100)
                .build();
```

Implementation using main Game class
------------------------------------

Main core class:
```java
public class MyApp extends Game {
    private final List<Toast> toasts = new LinkedList<Toast>();
    private Toast.ToastFactory toastFactory;

    @Override
    public void create() {
        // load skin and font
        Skin skin = new Skin(Gdx.files.internal("my_skin/skin.json"));
        BitmapFont font = skin.getFont("my_font");

        // create factory
        toastFactory = new Toast.ToastFactory.Builder()
                        .font(font)
                        .build();
    }

    /**
    * Displays long toast
    */
    public void toastLong(String text) {
        toasts.add(toastFactory.create(text, Toast.Length.LONG));
    }

    /**
    * Displays short toast
    */
    public void toastShort(String text) {
        toasts.add(toastFactory.create(text, Toast.Length.SHORT));
    }

    @Override
    public void render() {
        super.render();

        // handle toast queue and display
        Iterator<Toast> it = toasts.iterator();
        while(it.hasNext()) {
            Toast t = it.next();
            if (!t.render(Gdx.graphics.getDeltaTime())) {
                it.remove(); // toast finished -> remove
            } else {
                break; // first toast still active, break the loop
            }
        }
    }
}
```

Then you can create toast by simply calling `game.toastShort("Hello world!");` where `game` is reference to your instance of `MyApp` class

This implementation uses FIFO queue so toasts are displayed in the insertion order one by one