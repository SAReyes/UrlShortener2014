

# Project "GoldenPoppy"

Golden poppy is a tone of gold that is the color of the California poppy—the official state flower of California—the Golden State. The first recorded use of golden poppy as a color name in English was in 1927.

# Team Members

* Adrián Gañan del Prado (leader)
* Javier Tello Alquézar
* Andrés Julián López

# Features implemented

* [isAlive](https://github.com/adriangp/UrlShortener2014/tree/master/goldenPoppy/src/main/java/urlshortener2014/goldenPoppy/isAlive): Service that checks that an URL is alive. Service is at [http://localhost:8080/isAlive.html](http://localhost:8080/isAlive.html). An automatic test is at [http://localhost:8080/isAliveTest.html](http://localhost:8080/isAliveTest.html)

* [Interstitial](https://github.com/adriangp/UrlShortener2014/tree/master/goldenPoppy/src/main/java/urlshortener2014/goldenPoppy/intesicial): This service associates an `url` to a `sponsor`. Then, when someone click on the `short url`, will appear the `sponsor` during 10 seconds. After this time, will be redirected to the `target` page. Run the JUnit test by typing `gradle test`

* [Massive load](https://github.com/adriangp/UrlShortener2014/tree/master/goldenPoppy/src/main/java/urlshortener2014/goldenPoppy/massiveLoad): This service allows to the users to load a file with a lot of long URLs to short them. Periodically, the service notify to the user about the progress. Finally, the service allow to the user to get a file with the long URLs and the short URL associated to it.

* [QR generator](https://github.com/adriangp/UrlShortener2014/tree/master/goldenPoppy/src/main/java/urlshortener2014/goldenPoppy/qr): This service returns an image. That image is a QR code wich could be read with a device with an integrated camera, and is identified with the previous given URL. To check that this service works: first start the server running `gradle start` then ask for an URL at the endpoint `/qr/URL`. Once you have the image, you should compare it with Google QR generator API and check that both are the same image.
