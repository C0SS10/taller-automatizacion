function fn() {
  var env = karate.env || "dev";
  var config = {
    baseUrl: "https://thinking-tester-contact-list.herokuapp.com",
  };

  if (env === "dev") {
    config.baseUrl = "https://thinking-tester-contact-list.herokuapp.com";
  } else if (env === "test") {
    config.baseUrl = "https://thinking-tester-contact-list.herokuapp.com";
  }

  return config;
}
