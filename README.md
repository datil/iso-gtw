# iso-gtw

__iso-gtw__ is an ISO 8583 gateway for the internet era. It connects to any ISO 8583 host and allows you to send and receive messages over HTTP and JSON.

## Getting Started

1. Start the application: `lein run-dev` \*
2. Go to [localhost:8080](http://localhost:8080/) to see: `Hello World!`
3. Read your app's source code at src/iso_gtw/service.clj. Explore the docs of functions
   that define routes and responses.
4. Run your app's tests with `lein test`. Read the tests at test/iso_gtw/service_test.clj.
5. Learn more! See the [Links section below](#links).

\* `lein run-dev` automatically detects code changes. Alternatively, you can run in production mode
with `lein run`.

## Configuration

To configure logging see config/logback.xml. By default, the app logs to stdout and logs/.
To learn more about configuring Logback, read its [documentation](http://logback.qos.ch/documentation.html).

## Links
* [About ISO 8583](https://en.wikipedia.org/wiki/ISO_8583)
* [j8583](http://j8583.sourceforge.net/)
* [JReactive-8583](https://github.com/kpavlov/jreactive-8583)
* [Pedestal](https://github.com/pedestal/pedestal)
