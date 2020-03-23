/**
 * 入口页
 */
import React from 'react';
import ReactDOM from 'react-dom';
import { createStore, combineReducers, compose, applyMiddleware } from 'redux';
import { routerReducer } from 'react-router-redux';
import thunk from 'redux-thunk';
import { Provider, connect } from 'react-redux';
import { HashRouter, Route, Switch, Redirect } from 'react-router-dom';
import { ConfigProvider, Loading } from '@alifd/next';

import './lib';

import Layout from './layouts/MainLayout';
import { LANGUAGE_KEY, REDUX_DEVTOOLS } from './constants';

import Login from './pages/Login';
import Namespace from './pages/NameSpace';
import Password from './pages/Password';
import Newconfig from './pages/ConfigurationManagement/NewConfig';
import Configsync from './pages/ConfigurationManagement/ConfigSync';
import Configdetail from './pages/ConfigurationManagement/ConfigDetail';
import Configeditor from './pages/ConfigurationManagement/ConfigEditor';
import HistoryDetail from './pages/ConfigurationManagement/HistoryDetail';
import ConfigRollback from './pages/ConfigurationManagement/ConfigRollback';
import HistoryRollback from './pages/ConfigurationManagement/HistoryRollback';
import ListeningToQuery from './pages/ConfigurationManagement/ListeningToQuery';
import ConfigurationManagement from './pages/ConfigurationManagement/ConfigurationManagement';
import ServiceList from './pages/ServiceManagement/ServiceList';
import ServiceDetail from './pages/ServiceManagement/ServiceDetail';
import SubscriberList from './pages/ServiceManagement/SubscriberList';
import DataDictList from './pages/DataDict/List';
import DataDictDetail from './pages/DataDict/Detail';
import Welcome from './pages/Welcome/Welcome';

import reducers from './reducers';
import { changeLanguage } from './reducers/locale';

import './index.scss';
import PropTypes from 'prop-types';

module.hot && module.hot.accept();

if (!localStorage.getItem(LANGUAGE_KEY)) {
  localStorage.setItem(LANGUAGE_KEY, navigator.language === 'en-US' ? 'en-US' : 'zh-CN');
}

const reducer = combineReducers({
  ...reducers,
  routing: routerReducer,
});

const store = createStore(
  reducer,
  compose(
    applyMiddleware(thunk),
    window[REDUX_DEVTOOLS] ? window[REDUX_DEVTOOLS]() : f => f
  )
);

const MENU = [
  { path: '/', exact: true, render: () => <Redirect to="/welcome" /> },
  { path: '/welcome', component: Welcome },
  { path: '/namespace', component: Namespace },
  { path: '/password', component: Password },
  { path: '/newconfig', component: Newconfig },
  { path: '/configsync', component: Configsync },
  { path: '/configdetail', component: Configdetail },
  { path: '/configeditor', component: Configeditor },
  { path: '/historyDetail', component: HistoryDetail },
  { path: '/configRollback', component: ConfigRollback },
  { path: '/historyRollback', component: HistoryRollback },
  { path: '/listeningToQuery', component: ListeningToQuery },
  { path: '/configurationManagement', component: ConfigurationManagement },
  { path: '/serviceManagement', component: ServiceList },
  { path: '/serviceDetail', component: ServiceDetail },
  { path: '/subscriberList', component: SubscriberList },
  { path: '/dataDictList', component: DataDictList },
  { path: '/dataDictDetail', component: DataDictDetail },
];

@connect(
  state => ({ ...state.locale }),
  { changeLanguage }
)
class App extends React.Component {
  static propTypes = {
    locale: PropTypes.object,
    changeLanguage: PropTypes.func,
  };

  constructor(props) {
    super(props);
    this.state = {
      shownotice: 'none',
      noticecontent: '',
      nacosLoading: {},
    };
  }

  componentDidMount() {
    const language = localStorage.getItem(LANGUAGE_KEY);
    this.props.changeLanguage(language);
  }

  get router() {
    return (
      <HashRouter>
        <Switch>
          <Route path="/login" component={Login} />
          <Layout>
            {MENU.map(item => (
              <Route key={item.path} {...item} />
            ))}
          </Layout>
        </Switch>
      </HashRouter>
    );
  }

  render() {
    const { locale } = this.props;
    return (
      <Loading
        className="nacos-loading"
        shape="flower"
        tip="loading..."
        visible={false}
        fullScreen
        {...this.state.nacosLoading}
      >
        <ConfigProvider locale={locale}>{this.router}</ConfigProvider>
      </Loading>
    );
  }
}

ReactDOM.render(
  <Provider store={store}>
    <App />
  </Provider>,
  document.getElementById('root')
);
