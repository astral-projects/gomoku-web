export type ApiResponse<T> = {
  contentType: string;
  json: T;
}

type Options = {
  method: string;
  body?: BodyInit;
} 

export default function () {
  return {
    getApi: getApi,
    postApi: postApi,
    deleteApi: deleteApi,
    putApi: putApi,
  };

  /**
   * Function that fetches the API and returns the response as a json
   * @param path - The path to the API
   * @param options - The options for the fetch
   */
  async function fetchApi<T>(path: string, options: Options): Promise<ApiResponse<T>> {
    const response = await fetch(path, options);
    const contentType = response.headers.get('content-type');
    const json = await response.json();
    return {
      contentType: contentType,
      json: json,
    };
  }
  
  /**
   * Function that fetches the API with a token and returns the response as a json
   * @param path
   */
  function getApi<T>(path: string): Promise<ApiResponse<T>> {
    const options = {
      // get method tag value
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
    };
    return fetchApi<T>(path, options);
  }
  
  /**
   * Function that fetches the API for POST requests and returns the response as a json
   * @param path
   * @param body
   */
  function postApi<T, R>(path: string, body: T): Promise<ApiResponse<R>> {
    const options = {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(body),
    };
    return fetchApi<R>(path, options);
  }
  
  /**
   * Function that fetches the API for DELETE requests and returns the response as a json
   * @param path
   */
  function deleteApi<T>(path: string): Promise<ApiResponse<T>> {
    const options = {
      method: 'DELETE',
    };
    return fetchApi<T>(path, options);
  }
  
  /**
   * Function that fetches the API for PUT requests and returns the response as a json
   * @param path
   * @param body
   */
  function putApi<T, R>(path: string, body: T): Promise<ApiResponse<R>> {
    const options = {
      method: 'PUT',
      body: JSON.stringify(body),
    };
    return fetchApi<R>(path, options);
  }
  
}
