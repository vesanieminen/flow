/**
 * NOTICE: this is an auto-generated file
 *
 * This file has been generated by the `flow:prepare-frontend` maven goal.
 * This file will be overwritten on every run. Any custom changes should be made to webpack.config.js
 */
const fs = require('fs');
const CopyWebpackPlugin = require('copy-webpack-plugin');
const CompressionPlugin = require('compression-webpack-plugin');
const {BabelMultiTargetPlugin} = require('webpack-babel-multi-target-plugin');

// Flow plugins
const StatsPlugin = require('@vaadin/stats-plugin');
const ApplicationThemePlugin = require('@vaadin/application-theme-plugin');

const path = require('path');
const baseDir = path.resolve(__dirname);
// the folder of app resources (main.js and flow templates)

// this matches /themes/my-theme/ and is used to check css url handling and file path build.
const themePartRegex = /(\\|\/)themes\1[\s\S]*?\1/;

const frontendFolder = '[to-be-generated-by-flow]';

const fileNameOfTheFlowGeneratedMainEntryPoint = '[to-be-generated-by-flow]';
const mavenOutputFolderForFlowBundledFiles = '[to-be-generated-by-flow]';

const devmodeGizmoJS = '[to-be-generated-by-flow]';

// public path for resources, must match Flow VAADIN_BUILD
const build = 'build';
// public path for resources, must match the request used in flow to get the /build/stats.json file
const config = 'config';
// folder for outputting index.js bundle, etc.
const buildFolder = `${mavenOutputFolderForFlowBundledFiles}/${build}`;
// folder for outputting stats.json
const confFolder = `${mavenOutputFolderForFlowBundledFiles}/${config}`;
// file which is used by flow to read templates for server `@Id` binding
const statsFile = `${confFolder}/stats.json`;

// Folders in the project which can contain static assets.
const projectStaticAssetsFolders = [
  path.resolve(__dirname, 'src', 'main', 'resources', 'META-INF', 'resources'),
  path.resolve(__dirname, 'src', 'main', 'resources', 'static'),
  frontendFolder
];

const projectStaticAssetsOutputFolder = [to-be-generated-by-flow];

// Folders in the project which can contain application themes
const themeProjectFolders = projectStaticAssetsFolders.map((folder) =>
  path.resolve(folder, 'themes')
);


// Target flow-fronted auto generated to be the actual target folder
const flowFrontendFolder = '[to-be-generated-by-flow]';

// make sure that build folder exists before outputting anything
const mkdirp = require('mkdirp');

const devMode = process.argv.find(v => v.indexOf('webpack-dev-server') >= 0);

!devMode && mkdirp(buildFolder);
mkdirp(confFolder);

let stats;

const transpile = !devMode || process.argv.find(v => v.indexOf('--transpile-es5') >= 0);

const watchDogPrefix = '--watchDogPort=';
let watchDogPort = devMode && process.argv.find(v => v.indexOf(watchDogPrefix) >= 0);
let client;
if (watchDogPort) {
  watchDogPort = watchDogPort.substr(watchDogPrefix.length);
  const runWatchDog = () => {
    client = new require('net').Socket();
    client.setEncoding('utf8');
    client.on('error', function () {
      console.log("Watchdog connection error. Terminating webpack process...");
      client.destroy();
      process.exit(0);
    });
    client.on('close', function () {
      client.destroy();
      runWatchDog();
    });

    client.connect(watchDogPort, 'localhost');
  }

  runWatchDog();
}

exports = {
  frontendFolder: `${frontendFolder}`,
  buildFolder: `${buildFolder}`,
  confFolder: `${confFolder}`
};

module.exports = {
  mode: 'production',
  context: frontendFolder,
  entry: {
    bundle: fileNameOfTheFlowGeneratedMainEntryPoint,
    ...(devMode && { gizmo: devmodeGizmoJS })
  },

  output: {
    filename: `${build}/vaadin-[name]-[contenthash].cache.js`,
    path: mavenOutputFolderForFlowBundledFiles,
    publicPath: 'VAADIN/',
  },

  resolve: {
    // Search for import 'x/y' inside these folders, used at least for importing an application theme
    modules: [
      'node_modules',
      flowFrontendFolder,
      ...projectStaticAssetsFolders,
    ],
    extensions: ['.ts', '.js'],
    alias: {
      Frontend: frontendFolder
    }
  },

  devServer: {
    // webpack-dev-server serves ./ ,  webpack-generated,  and java webapp
    contentBase: [mavenOutputFolderForFlowBundledFiles, 'src/main/webapp'],
    after: function(app, server) {
      app.get(`/stats.json`, function(req, res) {
        res.json(stats);
      });
      app.get(`/stats.hash`, function(req, res) {
        res.json(stats.hash.toString());
      });
      app.get(`/assetsByChunkName`, function(req, res) {
        res.json(stats.assetsByChunkName);
      });
      app.get(`/stop`, function(req, res) {
        // eslint-disable-next-line no-console
        console.log("Stopped 'webpack-dev-server'");
        process.exit(0);
      });
    }
  },

  module: {
    rules: [
      {
        test: /\.tsx?$/,
        use: ['ts-loader']
      },
      ...(transpile ? [{ // Files that Babel has to transpile
        test: /\.js$/,
        use: [BabelMultiTargetPlugin.loader()]
      }] : []),
      {
        test: /\.css$/i,
        use: [
          {
            loader: 'css-loader',
            options: {
              url: (url, resourcePath) => {
                // Only translate files from node_modules
                const resolve = resourcePath.match(/(\\|\/)node_modules\1/);
                const themeResource = resourcePath.match(themePartRegex) && url.match(/^themes\/[\s\S]*?\//);
                return resolve || themeResource;
              },
              // use theme-loader to also handle any imports in css files
              importLoaders: 1
            },
          },
          {
            // theme-loader will change any url starting with './' to start with 'VAADIN/static' instead
            // NOTE! this loader should be here so it's run before css-loader as loaders are applied Right-To-Left
            loader: '@vaadin/theme-loader',
            options: {
              devMode: devMode
            }
          }
        ],
      },
      {
        // File-loader only copies files used as imports in .js files or handled by css-loader
        test: /\.(png|gif|jpg|jpeg|svg|eot|woff|woff2|ttf)$/,
        use: [{
          loader: 'file-loader',
          options: {
            outputPath: 'static/',
            name(resourcePath, resourceQuery) {
              if (resourcePath.match(/(\\|\/)node_modules\1/)) {
                return /(\\|\/)node_modules\1(?!.*node_modules)([\S]+)/.exec(resourcePath)[2].replace(/\\/g, "/");
              }
              return '[path][name].[ext]';
            }
          }
        }],
      },
    ]
  },
  performance: {
    maxEntrypointSize: 2097152, // 2MB
    maxAssetSize: 2097152 // 2MB
  },
  plugins: [
    // Generate compressed bundles when not devMode
    ...(devMode ? [] : [new CompressionPlugin()]),

    // Transpile with babel, and produce different bundles per browser
    ...(transpile ? [new BabelMultiTargetPlugin({
      babel: {
        plugins: [
          // workaround for Safari 10 scope issue (https://bugs.webkit.org/show_bug.cgi?id=159270)
          "@babel/plugin-transform-block-scoping",

          // Edge does not support spread '...' syntax in object literals (#7321)
          "@babel/plugin-proposal-object-rest-spread"
        ],

        presetOptions: {
          useBuiltIns: false // polyfills are provided from webcomponents-loader.js
        }
      },
      targets: {
        'es6': { // Evergreen browsers
          browsers: [
            // It guarantees that babel outputs pure es6 in bundle and in stats.json
            // In the case of browsers no supporting certain feature it will be
            // covered by the webcomponents-loader.js
            'last 1 Chrome major versions'
          ],
        },
        'es5': { // IE11
          browsers: [
            'ie 11'
          ],
          tagAssetsWithKey: true, // append a suffix to the file name
        }
      }
    })] : []),

    new ApplicationThemePlugin({
      // The following matches target/flow-frontend/theme/theme-generated.js and not frontend/themes
      themeResourceFolder: path.resolve(flowFrontendFolder, 'theme'),
      themeProjectFolders: themeProjectFolders,
      projectStaticAssetsOutputFolder: projectStaticAssetsOutputFolder,
    }),

    new StatsPlugin({
      devMode: devMode,
      statsFile: statsFile,
      setResults: function (statsFile) {
        stats = statsFile;
      }
    }),

    // Generates the stats file for flow `@Id` binding.
    function (compiler) {
        compiler.hooks.done.tapAsync('FlowIdPlugin', (compilation, done) => {
          // trigger live reload via server
          if (client) {
            client.write('reload\n');
          }
          done();
        });
    },

    // Copy webcomponents polyfills. They are not bundled because they
    // have its own loader based on browser quirks.
    new CopyWebpackPlugin([{
      from: `${baseDir}/node_modules/@webcomponents/webcomponentsjs`,
      to: `${build}/webcomponentsjs/`
    }]),
  ]
};
