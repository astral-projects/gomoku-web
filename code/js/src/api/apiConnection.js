export default function () {
  return {
    getApi: getApi,
    postApi: postApi,
    deleteApi: deleteApi,
    putApi: putApi,
    getWithoutToken: getWithoutToken,
  };

  /**
   * Function that fetches the API and returns the response as a json
   * @param path - The path to the API
   * @param options - The options for the fetch
   */
  async function fetchApi(path, options) {
    return await fetch(path, options);
  }

  /**
   * Function that fetches the API without a token and returns the response as a json
   */
  function getWithoutToken(path) {
    const options = {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
      },
    };
    return fetchApi(path, options);
  }

  /**
   * Function that fetches the API with a token and returns the response as a json
   * This is used for GET requests
   * @param path
   * @param token - The token for the user
   */
  function getApi(path, token) {
    const options = {
      method: 'GET',
      headers: {
        'Authorization': token,
        'Content-Type': 'application/json',
        'Accept': 'application/json',
      },
    };
    return fetchApi(path, options);
  }

  /**
   * Function that fetches the API for POST requests and returns the response as a json
   * @param path
   * @param token
   * @param body
   */
  function postApi(path, token, body) {
    const options = {
      method: 'POST',
      headers: {
        'Authorization': token,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(body),
    };
    return fetchApi(path, options);
  }

  /**
   * Function that fetches the API for DELETE requests and returns the response as a json
   * @param path
   * @param token
   */
  function deleteApi(path, token) {
    const options = {
      method: 'Delete',
      headers: {
        'Authorization': token,
        'Content-Type': 'application/json',
      },
    };
    return fetchApi(path, options);
  }

  /**
   * Function that fetches the API for PUT requests and returns the response as a json
   *
   * @param path
   * @param token
   * @param body
   */
  function putApi(path, token, body) {
    const options = {
      method: 'PUT',
      headers: {
        'Authorization': token,
        'Content-Type': 'application/json',
        Accept: 'application/json',
      },
      body: JSON.stringify(body),
    };
    return fetchApi(path, options);
  }
}
